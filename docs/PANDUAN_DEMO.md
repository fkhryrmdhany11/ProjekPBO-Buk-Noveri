# Panduan kontrol dan demo 3–5 menit

## Presentasi paling mudah: Auto Demo

Pilih **Auto Demo** di menu utama. Jalur pengujian berlangsung sekitar 3 menit 25 detik, 44 tahap. Kamera tetap pada scene aktif; HUD memperlihatkan perubahan waktu, saldo, tas, friendship, BBM dan tujuan. Tekan I/Tas untuk melihat inventory bila ingin menjelaskan, lalu tutup dialog. Pause/dialog akan memperpanjang durasi presentasi.

Demo memperlihatkan Motor, perjalanan nyata, pembelian makanan, penolakan makanan salah, makanan sesuai, friendship 30 dan mengikuti pemain, suara Kucing/Burung, dua jenis hewan, pekerjaan NPC, plaza, antrean/pengisian SPBU, pulang, misi hari pertama berhasil, hari berikutnya, pilihan Mobil, dan ending hari kedua yang belum lengkap. **Stop Demo** mengembalikan state pemain sebelumnya. Save dinonaktifkan pada demo.

## Demo manual sekitar empat menit

Gunakan **Mulai Baru**, isi nama, dan pilih Motor. Waktu berikut merupakan panduan presentasi, bukan batas waktu permainan. Antrean/event acak dapat mengubah durasi.

| Waktu presentasi | Aksi dan hal yang ditunjukkan |
| --- | --- |
| 00.00–00.30 | E di rumah → pilih Motor → Berangkat. Tunjukkan Rp50.000, jam 07.00, BBM 7/10 L dan misi kosong. |
| 00.30–01.00 | Jalan ke kanan menuju simpang tengah, ke atas, lalu ke kanan ke pintu Taman. E untuk masuk. Kendaraan tetap di parkir; karakter berjalan. |
| 01.00–01.30 | Dekati toko kiri atas, E, beli satu Daging dan satu Biji-bijian. Saldo menjadi Rp35.000. Buka Tas untuk memperlihatkan stok. |
| 01.30–02.10 | Dekati Kucing kanan bawah, dengarkan suara dan beri Daging. Dekati Burung kiri bawah lalu beri Biji-bijian. HUD hewan 2/2; pulang belum terpenuhi. |
| 02.10–02.40 | Kembali ke parkir kiri bawah dan E keluar. Lewat jalan utara menuju pintu Pusat Kota. Dekati Pak Raka, bantu kios 4 detik: saldo +Rp15.000, waktu +25 menit. |
| 02.40–03.20 | Keluar kota, kembali melalui simpang tengah ke jalan selatan dan pintu SPBU. Tunggu antrean; E, nominal 10000, Hitung rincian, Konfirmasi. Tunjukkan pembayaran tepat dan progress 5 detik. |
| 03.20–04.00 | Ke Peta → jalan ke kiri menuju Rumah → E masuk → Akhiri hari. Tunjukkan misi berhasil dan ringkasan. Save; Hari Berikutnya memulai 07.00 sambil mempertahankan uang, tas, friendship dan BBM. |

Jika antrean/event memperpanjang waktu, lewatkan kunjungan SPBU manual dan jelaskan melalui Auto Demo. Untuk menunjukkan friendship 30 tanpa menghabiskan saldo, Auto Demo sudah membeli tiga Daging dan memberi Kucing tiga kali dengan cooldown.

## Kendali yang perlu diingat

WASD/panah menggerakkan kendaraan atau karakter. GUI arah bergerak terus sampai Stop; Spasi berhenti. E hanya berinteraksi ketika dekat target. I membuka Tas, P menjeda, Esc ke menu. Setelah mengklik tombol, key bindings tetap tersedia. Kunci arah yang ditahan saat Stop perlu dilepas sebelum dipakai kembali.

## Penjelasan singkat saat menunjukkan permainan

“Semua lokasi memakai satu GameState. Uang yang dipakai membeli makanan juga dipakai mengisi BBM. Hewan dipanggil melalui tipe Hewan, tetapi subclass menentukan makanan yang diterima dan suara. Kendaraan dipanggil melalui tipe Kendaraan, tetapi Motor dan Mobil menghasilkan kecepatan, konsumsi, dan kapasitas berbeda. Ringkasan hari hanya menyatakan berhasil setelah dua jenis hewan diberi makanan dan pemain pulang. Demo memiliki state sendiri sehingga tidak mengubah save pemain.”

## Coba perilaku gagal dengan aman

| Percobaan | Hasil yang benar |
| --- | --- |
| Beli Daging lebih banyak daripada saldo | Pesan gagal; uang/stok tetap |
| Beri Roti kepada Kucing | Friendship -2 sampai minimum 0; Roti tetap di tas |
| Beri makanan stok nol atau terlalu cepat | Ditolak; stok tidak negatif |
| Masuk lokasi dari jauh | Dekati pintu dahulu |
| Nominal SPBU 0, negatif, huruf atau terlalu besar | Pesan validasi; belum ditagih |
| Tangki hampir penuh | Hanya liter yang masuk yang ditagih |
| Batalkan pengisian | Uang/BBM tetap seperti sebelum pengisian |
| Pulang sebelum dua jenis hewan menerima makanan | Ending “misi belum lengkap” |
| Stop Demo | State pemain aktif kembali; save tidak berubah |
