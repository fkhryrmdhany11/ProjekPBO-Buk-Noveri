# Penjelasan PBO dari implementasi aktual

Nama package utama dipertahankan: `com.innoelektrica.polymorphism`. Contoh berikut menunjuk source yang disertakan dalam ZIP.

## Inheritance

`model/Hewan` merupakan abstract class untuk `Kucing`, `Anjing`, `Burung`, `Ayam`, dan `Angsa`. Nama, gambar, audio, dan informasi dasar diwariskan; setiap subclass menambahkan perilaku spesifik. `model/Kendaraan` menjadi induk `Motor` dan `Mobil`. `view/ScenePanel` menampung rendering, konversi koordinat, pencahayaan, karakter, kendaraan dan label yang dipakai `HomePanel`, `WorldMapPanel`, `ParkPanel`, `CityScenePanel`, serta `FuelStationPanel`.

## Overriding

| Induk dan method | Implementasi aktual subclass |
| --- | --- |
| `Hewan.getMakananFavorit()` | Kucing/Anjing: DAGING; Burung/Ayam: BIJI_BIJIAN; Angsa: ROTI |
| `Hewan.sapaan()` | Kelima hewan memiliki dialog khas |
| `Hewan.bersuara()` / `getSuara()` | Subclass memainkan WAV asli dan menentukan teks suara |
| `Kendaraan.bergerak()` | Motor 7, Mobil 5 piksel per tick dasar |
| `Kendaraan.getKapasitasTangki()` | Motor 10 L, Mobil 40 L |
| `Kendaraan.getKonsumsiPerTick()` | Motor 0,006 L, Mobil 0,026 L |
| `ScenePanel.paintWorld()` / `paintLabels()` | Setiap panel menggambar scene dan informasi sesuai lokasinya |

Anotasi `@Override` menunjukkan method yang menggantikan kontrak superclass. Data favorit makanan adalah aturan simulasi game.

## Polymorphism

Di `game/GameManager.feedAnimal()`, variabel `Hewan animal = animals.get(a)` dapat berisi objek dari kelima subclass. Pemanggilan `animal.menerima(food)` menjalankan method induk yang memanggil `getMakananFavorit()`; method favorit yang dijalankan ditentukan oleh objek subclass saat runtime. Panel tidak perlu memeriksa nama hewan untuk menentukan makanan.

Di `GameManager.tick()`, variabel `Kendaraan vehicle = state.getVehicle()` dipakai untuk `bergerak()` dan `getKonsumsiPerTick()`. Perhitungan jarak dan BBM memakai kontrak yang sama, sementara Motor dan Mobil menghasilkan kecepatan/konsumsi berbeda. `previewFuel()` juga memakai kapasitas subclass untuk membatasi ruang tangki.

`AnimalType.create()` dan `VehicleType.create()` adalah factory sederhana untuk memilih objek. Setelah objek dibuat, perilaku utama dipanggil melalui tipe induk. Pilihan objek pada enum berbeda perannya dari penentuan perilaku subclass pada runtime.

Inheritance saja belum membuktikan polymorphism. Saat menjelaskan kepada dosen, tunjukkan variabel bertipe induk serta pemanggilan method yang hasilnya berbeda untuk objek turunannya.

## Encapsulation dan transaksi

Field `GameState` privat. GUI membaca data melalui getter; setter paket dipakai logika dalam package `game`. `Inventory` menyimpan map privat dan mengembalikan tampilan baca melalui `getItems()`, sehingga panel tidak mengubah stok langsung. Perubahan normal dilakukan melalui `GameManager.buyFood()`, `feedAnimal()`, `emergencyFuel()`, dan `beginFuel()`.

Transaksi memeriksa syarat sebelum mengubah saldo/stok. `FuelQuote` merupakan record immutable untuk rincian BBM. `beginFuel()` menghitung ulang dan mencocokkan quote agar nominal lama/rekayasa atau klik ulang tidak membuat transaksi ganda. Selama pengisian, saldo/BBM belum berubah; selesai mengubah keduanya sekali, sedangkan `cancelFuel()` membatalkan tanpa perubahan.

`SaveService.fromProperties()` membuat state baru dan memeriksa seluruh nilai/relasi sebelum `MainFrame.loadGame()` mengganti state aktif. Save menggunakan file sementara; validasi atau penulisan gagal tidak merusak data permainan aktif.

## State bersama dan pemisahan tanggung jawab

`MainFrame` memiliki satu `GameManager`; `GameScreen` dan semua panel menerima `Supplier<GameManager>`. Supplier mengambil manager aktif, termasuk sesudah load. Panel menggambar dari `GameState` yang sama. Saldo, inventory, friendship dan kedua tangki tidak disalin ke masing-masing panel. `GameManager.changeLocation()` hanya mengubah lokasi/posisi dan progres yang relevan.

`GameManager` tidak mengimpor Swing, sehingga aturan bisnis dapat diuji tanpa jendela. `MainFrame` menangani menu/dialog; `ScenePanel` dan turunannya menangani gambar. `SaveService` menangani penyimpanan. `DemoController` memiliki manager dan state terpisah; `MainFrame.stopDemo()` memulihkan manager pemain.

Satu Timer 50 ms di `MainFrame` memanggil simulasi. Panel tidak membuat Timer tambahan. Pause/menu/dialog menahan simulasi. `InputController` memasang key bindings pada jendela dan meneruskan pelepasan tombol dari dialog modal. Audio dijalankan melalui satu worker daemon dan satu Clip oleh `AudioPlayer`.

## Urutan source yang mudah dipelajari

1. Baca `Hewan`, `Kucing`, `Burung`, `Kendaraan`, `Motor`, `Mobil` untuk kontrak dan override.
2. Baca `GameState` dan `Inventory` untuk data privat dan getter.
3. Baca `GameManager.buyFood()` dan `feedAnimal()` untuk validasi/transaksi.
4. Baca bagian gerak/BBM dalam `tick()` dan `previewFuel()` untuk polymorphism kendaraan.
5. Baca `MainFrame` → `GameScreen` → `ScenePanel` untuk hubungan GUI dengan state.
6. Baca `SaveService` dan pengujian `PersistenceProbe` untuk penyimpanan lintas proses.
