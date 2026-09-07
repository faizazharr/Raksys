package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.SshAuthMethod
import com.raksys.core.security.CredentialStore
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Parameters
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap

fun sshCredentialKey(profileId: String): String = "$profileId:ssh"

private class SshTunnel(private val ssh: SSHClient, private val serverSocket: ServerSocket) {
    fun close() {
        runCatching { serverSocket.close() }
        runCatching { ssh.disconnect() }
    }
}

/**
 * Opens a local port-forward through an SSH bastion so JDBC/Mongo/Redis drivers can connect to
 * "localhost:<forwardedPort>" instead of talking to the remote DB host directly.
 */
class SshTunnelManager(private val credentialStore: CredentialStore) {

    private val tunnels = ConcurrentHashMap<String, Pair<SshTunnel, Int>>()

    fun resolve(profile: ConnectionProfile): Pair<String, Int> {
        if (!profile.sshEnabled) return profile.host to profile.port
        val (_, localPort) = tunnels.getOrPut(profile.id) { openTunnel(profile) }
        return "localhost" to localPort
    }

    private fun openTunnel(profile: ConnectionProfile): Pair<SshTunnel, Int> {
        val ssh = SSHClient()
        ssh.addHostKeyVerifier(knownHostsVerifier())
        ssh.connect(profile.sshHost, profile.sshPort)

        when (profile.sshAuthMethod) {
            SshAuthMethod.PASSWORD -> {
                val sshPassword = credentialStore.get(sshCredentialKey(profile.id)).orEmpty()
                ssh.authPassword(profile.sshUsername, sshPassword)
            }
            SshAuthMethod.PRIVATE_KEY -> {
                ssh.authPublickey(profile.sshUsername, profile.sshPrivateKeyPath)
            }
        }

        val serverSocket = ServerSocket(0, 0, InetAddress.getByName("localhost"))
        val localPort = serverSocket.localPort
        val params = Parameters("localhost", localPort, profile.host, profile.port)
        val forwarder = ssh.newLocalPortForwarder(params, serverSocket)

        Thread({ runCatching { forwarder.listen() } }, "ssh-tunnel-${profile.id}")
            .apply { isDaemon = true }
            .start()

        return SshTunnel(ssh, serverSocket) to localPort
    }

    /**
     * Verifies the SSH server's host key against the user's own `~/.ssh/known_hosts` — the same
     * trust store the system `ssh` client uses. If the file is missing or the host isn't in it yet,
     * the connection is refused rather than silently trusting an unverified key (no MITM bypass);
     * the user needs to `ssh` into the host once from a terminal to add it, same as any SSH client.
     */
    private fun knownHostsVerifier(): OpenSSHKnownHosts {
        val knownHostsFile = File(System.getProperty("user.home"), ".ssh/known_hosts")
        check(knownHostsFile.exists()) {
            "~/.ssh/known_hosts not found. Run `ssh <user>@<host>` from a terminal once to trust " +
                "this host's key, then retry the connection."
        }
        return OpenSSHKnownHosts(knownHostsFile)
    }

    fun invalidate(profileId: String) {
        tunnels.remove(profileId)?.first?.close()
    }

    fun closeAll() {
        tunnels.values.forEach { it.first.close() }
        tunnels.clear()
    }
}
