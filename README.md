# A Day in My City

Game cerita simulasi kota Java Swing untuk showcase PBO. Dikembangkan dari proyek `_PolymorphismSimulator`: package `com.innoelektrica.polymorphism`, entry point `Main`, abstract class `Hewan` dan `Kendaraan`, subclass hewan/kendaraan, serta seluruh 11 gambar dan 5 suara asli dipakai kembali.

Pemain memulai hari pukul 07.00 di rumah dengan Rp50.000, tas kosong, dan kedua tangki berisi 70%. Tujuan harian: mengunjungi taman, memberi makanan sesuai kepada dua jenis hewan berbeda, kemudian pulang. Pulang sebelum selesai tetap diperbolehkan; ringkasan menyebut misi belum lengkap.

## Jalankan di Windows — JDK 17 atau lebih baru

1. Ekstrak seluruh ZIP. Buka folder `ADayInMyCity` hasil ekstraksi.
2. Pastikan JDK 17+ terpasang. Di Command Prompt, `java -version` harus menampilkan 17 atau lebih baru. Atur `JAVA_HOME` ke folder JDK atau tambahkan folder `bin` JDK ke `PATH` jika diperlukan.
3. Klik dua kali **`run-direct.bat`** untuk menjalankan JAR final yang sudah disertakan.

Untuk membangun ulang source sekaligus bermain, gunakan **`run.bat`**. Build offline tidak memerlukan Maven, dependensi tambahan, atau executable `javac`/`jar` di PATH; `tools/Build.java` memakai compiler milik JDK. Gunakan JDK, bukan JRE, untuk build.

Alternatif dari terminal dalam folder proyek:

```bat
java -jar dist\ADayInMyCity.jar
java tools\Build.java
java tools\Build.java --test
java -Djava.awt.headless=true -jar dist\ADayInMyCity.jar --check-resources
```

- `build.bat`: build dan periksa seluruh resource JAR.
- `test.bat`: build bersih dan jalankan pengujian otomatis.
- `run.ps1`: jalankan JAR dari PowerShell; `-Rebuild` membangun ulang dahulu.
- Linux/macOS dengan desktop: `./build.sh`, lalu `java -jar dist/ADayInMyCity.jar`.
- Maven tetap disediakan: `mvn clean package`, lalu `java -jar target/ADayInMyCity.jar`. Jalur Maven belum diuji di lingkungan pengembangan ini. Pengujian regresi mandiri dijalankan melalui `java tools/Build.java --test`.
- VS Code: buka folder proyek; gunakan extension Java bila tersedia. Konfigurasi sumber dan output disertakan di `.vscode/settings.json`.

JAR mandiri juga dapat dijalankan melalui `java -jar ADayInMyCity.jar`; seluruh resource sudah berada di dalamnya. GUI memerlukan lingkungan desktop.

## Kontrol

| Kendali | Aksi |
| --- | --- |
| WASD / tombol panah | Mengemudi di jalan atau berjalan di taman/kota |
| Tombol GUI Atas/Kiri/Kanan/Bawah | Gerak berkelanjutan sampai Stop |
| Spasi / Stop | Berhenti; tombol yang masih ditahan perlu dilepas untuk bergerak lagi |
| E / Interaksi | Masuk pintu, toko, hewan, NPC, plaza, atau rumah yang dekat |
| I / Tas | Lihat inventory dan friendship dari seluruh lokasi |
| P / Jeda | Hentikan waktu dan simulasi; tekan kembali untuk melanjutkan |
| Esc / Menu | Menu utama; permainan aktif tetap tersedia melalui Lanjutkan |
| Simpan / Muat | Save manual satu slot / pulihkan save |
| Bantuan | Pemulihan BBM ketika kurang dari 20% |
| Ke Peta | Keluar melalui pintu taman/kota, atau keluar SPBU |
| Auto Demo / Stop Demo | Presentasi otomatis dengan state terpisah |

Pintu lokasi berupa lingkaran pada peta. Dekati sampai petunjuk E muncul. Kendaraan hanya dapat bergerak di jalan dan dibatasi peta. Di taman, kendaraan berada di parkir; karakter berjalan mendekati hewan/toko. Gerak diagonal dinormalisasi. Klik objek juga dapat membuka interaksi setelah karakter cukup dekat. Keluar taman/kota harus dari pintu parkir.

## Aturan permainan

**Waktu.** Satu detik simulasi = dua menit permainan. Pagi 06.00–10.59, Siang 11.00–13.59, Sore 14.00–17.59, Malam 18.00–05.59. Toko buka 07.00–18.00. Hewan beristirahat dan menolak elusan pada malam hari; interaksi/dialog menyesuaikan suasana. Jam berhenti di 23.59 sampai pemain mengakhiri hari di rumah. Pause, menu, dialog, dan kehilangan fokus jendela menghentikan simulasi. Setelah jendela kembali aktif, tekan P untuk melanjutkan jika dijeda otomatis.

**Makanan simulasi.** Kucing/Anjing menerima Daging (Rp10.000), Burung/Ayam menerima Biji-bijian (Rp5.000), Angsa menerima Roti (Rp7.000). Ini aturan permainan. Pembelian 1–99 item per transaksi; tas maksimum 999 per jenis. Saldo/stok hanya berubah jika transaksi valid. Makanan sesuai: stok -1, friendship +10 (maksimum 100), progres jenis hewan diperbarui. Salah: stok tetap, friendship -2 (minimum 0), reaksi penolakan. Elus +2. Cooldown 2 detik mencegah pengulangan cepat. Friendship minimal 30 membuat hewan menyapa dan mengikuti pemain selama 8 detik setelah interaksi pada siang hari.

**Kendaraan.** Motor 10 L bergerak 7 piksel per tick dasar 50 ms dan memakai 0,006 L per 7 piksel perjalanan. Mobil 40 L bergerak 5 piksel dan memakai 0,026 L per 5 piksel. Perhitungan memakai jarak yang benar-benar ditempuh; idle, collision, taman, dialog, menu, dan pause tidak menghabiskan BBM. Di bawah 10% muncul satu peringatan per hari. Pada nol kendaraan berhenti.

**SPBU.** Antrean acak 0–3 kendaraan; setiap kendaraan 2 detik. Harga tetap Rp10.000/L, satuan transaksi 1 ml/Rp10. Masukkan rupiah tanpa titik/koma → Hitung rincian → Konfirmasi & isi. Nominal dibatasi saldo dan ruang tangki. Rincian menunjukkan volume, biaya, BBM serta saldo sebelum/sesudah. Pengisian 5 detik; uang dan BBM diubah tepat sekali saat selesai. Pembatalan sebelum selesai tidak menagih dan tidak menambahkan BBM. Klik ganda dan rincian lama ditolak. Penutupan aplikasi membatalkan pengisian aktif; tidak ada autosave.

**Bantuan.** Ketika BBM <20%, bantuan menambah 20% kapasitas tangki. Jika saldo ≥Rp5.000, biaya Rp5.000 dan waktu +60 menit. Jika saldo kurang, subsidi gratis dengan waktu +90 menit. Bantuan tetap tersedia saat jam mencapai 23.59 agar perjalanan dapat dilanjutkan.

**Pusat kota.** Pak Raka menawarkan pekerjaan 4 detik dengan upah Rp15.000 dan waktu +25 menit, sekali per hari. Jelajah plaza menambah 15 menit dan progres eksplorasi, sekali per hari.

**Event perjalanan.** Satu kesempatan setiap 8 detik mengemudi nyata: normal 70%, lampu merah 10% (berhenti 3 detik, +3 menit), anjing menyeberang 10% (berhenti 2 detik, +2 menit), kemacetan 10% (setengah kecepatan 5 detik, +8 menit). Peluang tersebut berlaku per kesempatan event. Event berakhir otomatis.

**Ending.** Di rumah, pilih Akhiri hari. Ringkasan mencatat hewan, makanan, saldo, eksplorasi, bantuan, dan aktivitas. Misi berhasil hanya ketika taman dikunjungi, minimal dua jenis hewan menerima makanan, dan pemain pulang. Save dan Hari Berikutnya tersedia. Hari berikutnya dimulai 07.00; uang, stok, friendship, pilihan kendaraan dan isi kedua tangki dipertahankan. Progres misi, pekerjaan/plaza, cooldown, log, peringatan, dan statistik harian direset; kendaraan dapat dipilih ulang sebelum berangkat.

## Save dan Auto Demo

Save berada di `%USERPROFILE%\.a-day-in-my-city\save.properties`. Gunakan tombol Simpan sebelum menutup aplikasi. Tidak ada autosave. Nama, hari/waktu, lokasi/posisi, saldo, tas, kendaraan, kedua tangki dan pecahan konsumsi, friendship/cooldown, progres, status hari, flags, dan aktivitas disimpan sebagai Properties UTF-8. Penulisan melalui file sementara kemudian penggantian atomik jika filesystem mendukungnya, dengan fallback penggantian biasa. Load divalidasi lengkap sebelum permainan aktif diganti. File hilang/rusak atau data di luar batas menghasilkan pesan; permainan aktif tetap utuh saat load gagal.

Save/load diblokir selama antrean, pengisian, pekerjaan kota, event aktif, dan Auto Demo. Dialog transaksi menahan waktu sehingga rincian tetap konsisten.

Auto Demo menjalankan 44 tahap melalui gerakan, collision, inventory, dan transaksi yang sama. Durasi pengujian sekitar **204,9 detik / 3 menit 25 detik**. Hari pertama berhasil; hari kedua mencoba Mobil dan menampilkan ending belum lengkap. Stop Demo mengembalikan permainan yang sebelumnya aktif. Demo tidak dapat menyimpan dan tidak mengubah file save pemain.

## Dokumentasi dan pengujian

- [Panduan kontrol dan demo 3–5 menit](docs/PANDUAN_DEMO.md)
- [Penjelasan PBO dengan class/method aktual](docs/PENJELASAN_PBO.md)
- [Daftar file dan fungsi perubahan](docs/DAFTAR_PERUBAHAN.md)
- [Hasil pengujian serta pemeriksaan manual](docs/HASIL_PENGUJIAN.md)
- [Log build terakhir](docs/BUILD_TEST_LOG.txt)

Build bersih JDK 17 berhasil tanpa error/warning compiler. **233 pemeriksaan otomatis lulus**, dijalankan terhadap JAR hasil build. Resource 11 PNG/5 WAV terbaca dan audio terdekode. Save diuji tulis/tutup proses/buka proses baru/load dalam dua JVM terpisah. Komponen Swing dirender headless pada 980×720 dan 1366×850.

GUI desktop interaktif, keluaran speaker, launcher Windows/PowerShell, dan Maven belum dijalankan langsung pada lingkungan ini. Screenshot di `docs/screenshots` adalah render komponen Swing headless; langkah manual ada di laporan pengujian.

## Struktur untuk dipelajari

| Bagian | Tanggung jawab |
| --- | --- |
| `model` | Fondasi Hewan/Kendaraan, subclass, enum makanan/hewan/kendaraan |
| `game/GameState`, `Inventory` | Satu data pemain; field privat dan akses baca |
| `game/GameManager` | Gerak, waktu, scene, transaksi, friendship, event, ending |
| `game/SaveService` | Serialisasi, validasi, penulisan sementara dan load |
| `game/DemoController` | State demo terpisah, rute 44 tahap |
| `view/MainFrame` | Menu/dialog, satu Timer 50 ms, penghubung aksi GUI |
| `view/GameScreen`, `ScenePanel` dan panel turunannya | HUD, layout adaptif dan gambar scene dari state bersama |
| `view/InputController` | Key bindings, stop, release saat dialog |
| `util/Resources`, `AudioPlayer` | Cache gambar/sprite; satu worker audio dan satu Clip |
| `tools/Build.java`, `JarBuilder.java` | Build offline Java 17, pemeriksaan resource, JAR executable |
| `src/test/java` | Pengujian logika, lintas JVM, demo, key bindings dan render |

Tidak ada timer di panel scene. Satu Timer MainFrame menggerakkan scene aktif; menu/pause/dialog menahannya. Audio berjalan di worker daemon dan dihentikan saat dialog/lokasi berubah atau aplikasi ditutup. Tidak ada `Thread.sleep()` pada EDT. Terdapat 40 class/enum/interface/record utama dan dua file pengujian.
