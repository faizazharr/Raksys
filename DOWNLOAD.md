# Mengunduh & Menginstal Raksys

Panduan instalasi **Raksys** pada macOS dan Windows tanpa perlu build dari source. Untuk Linux atau pengembangan, lihat bagian [Jalankan dari Source](#4-jalankan-dari-source-linux--developer).

---

## 1. Unduh Installer

1. Buka halaman [Releases](../../releases) pada repository ini.
2. Di bagian **Assets**, unduh file sesuai sistem operasi kamu:
   - **macOS** → `Raksys-<versi>.dmg`
   - **Windows** → `Raksys-<versi>.exe`

---

## 2. Instalasi

### 🍎 macOS (Drag-and-Drop)

1. Buka file **`Raksys-<versi>.dmg`**.
2. **Seret (drag)** ikon **Raksys.app** ke folder **Applications**.
3. *Eject* disk image dari Finder.

**Pembukaan pertama kali** — jika muncul peringatan Gatekeeper (aplikasi dari developer yang belum diverifikasi):

1. **Klik kanan** (atau *Control-click*) pada `Raksys.app` di folder Applications.
2. Pilih **Open** (Buka).
3. Klik **Open** pada kotak dialog konfirmasi.

*(Langkah ini hanya perlu dilakukan 1 kali saja.)*

Jika masih diblokir: **System Settings → Privacy & Security**, gulir ke bawah, lalu klik **Open Anyway** di samping nama Raksys.

### 🪟 Windows

1. Jalankan **`Raksys-<versi>.exe`** dan ikuti wizard instalasi.
2. Jika muncul layar biru **"Windows protected your PC"** (SmartScreen), klik **More info → Run anyway**.
3. Buka Raksys dari **Start Menu**.

---

## 3. Langkah Pertama Setelah Terpasang

1. Klik **+ Buat Koneksi** (atau tekan `⌘K` / `Ctrl+K` lalu pilih **Tambah Koneksi Baru**).
2. **Langkah 1** — pilih tipe database: PostgreSQL, MySQL, SQLite, MongoDB, atau Redis.
3. **Langkah 2** — isi detail koneksi:

   | Engine | Yang perlu diisi | Port default |
   |---|---|---|
   | PostgreSQL | Host, port, nama database, username, password | `5432` |
   | MySQL / MariaDB | Host, port, nama database, username, password | `3306` |
   | SQLite | Path file `.db` / `.sqlite` (dibuat otomatis jika belum ada) | — |
   | MongoDB | Host, port, **nama database (wajib)**, username, password | `27017` |
   | Redis | Host, port, index database (0–15), password | `6379` |

4. Pilih **Environment Tag** (`DEV`, `STG`, atau `PROD`). Tandai server produksi sebagai **PROD** agar kueri destruktif meminta konfirmasi.
5. Klik **⚡ Uji Koneksi** untuk memastikan semuanya benar, lalu **Simpan Koneksi**.
6. Pilih koneksi di sidebar kiri untuk mulai bekerja.

> 💡 Belum punya database? Aktifkan **"Buat Database Baru di Server Ini"** (PostgreSQL / MySQL) atau gunakan **Setup Cepat** untuk database lokal.

### Akses via SSH Tunnel (opsional)

Aktifkan **Akses via SSH Tunnel (Bastion)** lalu isi SSH host, username, dan password atau path private key.

> ⚠️ Raksys memverifikasi host key SSH terhadap `~/.ssh/known_hosts`. Jika muncul error `known_hosts not found` atau host tidak dikenal, jalankan sekali dari terminal:
> ```bash
> ssh <user>@<bastion-host>
> ```
> lalu ketik `yes` untuk mempercayai host tersebut, kemudian ulangi koneksi di Raksys.

### Pintasan Keyboard

| Pintasan | Fungsi |
|---|---|
| `⌘K` / `Ctrl+K` | Buka Command Palette |
| `⌘B` / `Ctrl+B` | Tampilkan / sembunyikan sidebar koneksi |
| `⌘↵` / `Ctrl+↵` | Jalankan SQL di SQL Console |

---

## 4. Jalankan dari Source (Linux / Developer)

Prasyarat: **JDK 17+** dan Git.

```bash
git clone https://github.com/faizazharr/Raksys.git
cd Raksys
./gradlew :app:run
```

Untuk membuat installer sendiri di OS yang sedang dipakai:

```bash
./gradlew :app:packageDistributionForCurrentOS
# Hasil: app/build/compose/binaries/main/
```

> 🐧 **Linux**: pastikan ada layanan *Secret Service* yang berjalan (mis. GNOME Keyring atau KWallet) agar password bisa disimpan aman.

---

## 5. Data & Privasi

| Data | Lokasi |
|---|---|
| Profil koneksi (tanpa password) | `~/.raksys/connections.json` |
| Password database & SSH | Keyring OS (Apple Keychain / Windows Credential Manager / Secret Service) |

Raksys **tidak mengirim telemetri** dan tidak terhubung ke server mana pun selain database (dan SSH bastion) yang kamu atur sendiri.

**Uninstall**: hapus aplikasi seperti biasa. Untuk menghapus data lokal, hapus folder `~/.raksys` dan entri **`com.raksys.dbtool`** di keyring OS.

---

## 6. Troubleshooting

| Masalah | Solusi |
|---|---|
| *Gak bisa connect … Connection refused* | Server belum jalan, atau host/port salah. |
| *Host … gak ditemukan* | Periksa penulisan host / DNS / VPN. |
| *Koneksi … timeout* | Periksa firewall, VPN, atau beban server. |
| *Username atau password salah* | Periksa kredensial; edit koneksi lalu simpan ulang password. |
| *Gagal negosiasi SSL/TLS* | Matikan opsi SSL jika server tidak mewajibkannya. |
| *`~/.ssh/known_hosts not found`* | Jalankan `ssh <user>@<host>` sekali dari terminal. |
| Password tidak tersimpan (Linux) | Pastikan Secret Service / keyring berjalan. |

Masih bermasalah? Buka [issue baru](../../issues/new?template=bug_report.md).
