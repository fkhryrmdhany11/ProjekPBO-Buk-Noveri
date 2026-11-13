# Daftar file dan fungsi perubahan

Dibandingkan dengan ZIP proyek asli. Terdapat **16 file Java utama lama yang dikembangkan**, **24 file Java utama baru**, dan **2 file pengujian baru**. Total source utama **40 file**. Package dan fondasi PBO dipertahankan.

| File | Status | Fungsi perubahan |
| --- | --- | --- |
| `.gitignore` | Diubah | Abaikan output build, target Maven dan file sementara. |
| `.vscode/settings.json` | Diubah | Sumber/output Java di VS Code. |
| `README.md` | Diubah | Instruksi menjalankan, gameplay, kontrol, state, struktur dan batas pengujian. |
| `build.bat` | Ditambah | Build offline Windows dengan JAVA_HOME/PATH. |
| `build.sh` | Ditambah | Launcher build POSIX. |
| `pom.xml` | Diubah | Maven Java 17, artifact baru dan manifest Main-Class. |
| `run-direct.bat` | Diubah | Jalankan JAR final tanpa build ulang. |
| `run.bat` | Diubah | Build lalu jalankan JAR di Windows. |
| `run.ps1` | Diubah | Launcher PowerShell dengan opsi Rebuild. |
| `src/main/java/com/innoelektrica/polymorphism/Main.java` | Diubah | Entry point game; verifikasi resource CLI dan penanganan desktop/headless. |
| `src/main/java/com/innoelektrica/polymorphism/game/ActionResult.java` | Ditambah | Hasil logika transaksi beserta pesan untuk GUI. |
| `src/main/java/com/innoelektrica/polymorphism/game/DayStatus.java` | Ditambah | Hari aktif atau sudah diakhiri. |
| `src/main/java/com/innoelektrica/polymorphism/game/DemoController.java` | Ditambah | Rute 44 tahap dengan gerak/transaksi nyata pada state terpisah. |
| `src/main/java/com/innoelektrica/polymorphism/game/FuelQuote.java` | Ditambah | Record immutable preview pengisian dan snapshot sebelum transaksi. |
| `src/main/java/com/innoelektrica/polymorphism/game/GameManager.java` | Ditambah | Aturan gerak/waktu, transaksi, friendship, event, misi, ending dan reset harian. |
| `src/main/java/com/innoelektrica/polymorphism/game/GameState.java` | Ditambah | Satu state pemain lengkap; getter, flags, kedua tangki dan log. |
| `src/main/java/com/innoelektrica/polymorphism/game/Inventory.java` | Ditambah | Stok privat, batas 999 dan tampilan map baca. |
| `src/main/java/com/innoelektrica/polymorphism/game/Location.java` | Ditambah | Lima lokasi dan label Indonesia. |
| `src/main/java/com/innoelektrica/polymorphism/game/ResourceVerifier.java` | Ditambah | Pemeriksaan 11 PNG/5 WAV dari classpath/JAR. |
| `src/main/java/com/innoelektrica/polymorphism/game/SaveService.java` | Ditambah | Properties UTF-8, validasi lengkap dan write melalui file sementara. |
| `src/main/java/com/innoelektrica/polymorphism/game/TimePeriod.java` | Ditambah | Empat periode pencahayaan/aktivitas dari waktu permainan. |
| `src/main/java/com/innoelektrica/polymorphism/game/TrafficEvent.java` | Ditambah | Peluang event 70/10/10/10 dan dampaknya. |
| `src/main/java/com/innoelektrica/polymorphism/game/WorldLayout.java` | Ditambah | Jalan, pintu, batas, kolam/toko dan koordinat interaksi. |
| `src/main/java/com/innoelektrica/polymorphism/model/Angsa.java` | Diubah | Override Roti, sapaan dan WAV Angsa. |
| `src/main/java/com/innoelektrica/polymorphism/model/AnimalType.java` | Ditambah | Identitas stabil dan factory lima subclass hewan. |
| `src/main/java/com/innoelektrica/polymorphism/model/Anjing.java` | Diubah | Override makanan Daging, sapaan dan WAV Anjing. |
| `src/main/java/com/innoelektrica/polymorphism/model/Ayam.java` | Diubah | Override Biji-bijian, sapaan dan WAV Ayam. |
| `src/main/java/com/innoelektrica/polymorphism/model/Burung.java` | Diubah | Override Biji-bijian, sapaan dan WAV Burung. |
| `src/main/java/com/innoelektrica/polymorphism/model/FoodType.java` | Ditambah | Label/harga tiga makanan simulasi. |
| `src/main/java/com/innoelektrica/polymorphism/model/Hewan.java` | Diubah | Kontrak makanan, sapaan, dialog sesuai waktu/friendship dan reaksi; suara lama dipertahankan. |
| `src/main/java/com/innoelektrica/polymorphism/model/Kendaraan.java` | Diubah | Kontrak induk pergerakan dan BBM tetap digunakan oleh gameplay. |
| `src/main/java/com/innoelektrica/polymorphism/model/Kucing.java` | Diubah | Override makanan Daging, sapaan dan WAV Kucing. |
| `src/main/java/com/innoelektrica/polymorphism/model/Mobil.java` | Diubah | Kapasitas 40 L, gerak 5 piksel, konsumsi lebih boros. |
| `src/main/java/com/innoelektrica/polymorphism/model/Motor.java` | Diubah | Kapasitas 10 L, gerak 7 piksel, konsumsi lebih hemat. |
| `src/main/java/com/innoelektrica/polymorphism/model/VehicleType.java` | Ditambah | Identitas stabil dan factory Motor/Mobil. |
| `src/main/java/com/innoelektrica/polymorphism/util/AudioPlayer.java` | Diubah | Satu worker/Clip; play/stop tanpa memblokir EDT dan pesan kegagalan perangkat. |
| `src/main/java/com/innoelektrica/polymorphism/util/Resources.java` | Ditambah | Cache gambar, trim transparansi sprite dan drawFit berasio asli. |
| `src/main/java/com/innoelektrica/polymorphism/view/CityScenePanel.java` | Diubah | NPC, kios, plaza, parkir dan progress aktivitas. |
| `src/main/java/com/innoelektrica/polymorphism/view/FuelStationPanel.java` | Diubah | Antrean bergerak, kendaraan aktif, tangki dan progress pengisian. |
| `src/main/java/com/innoelektrica/polymorphism/view/GameScreen.java` | Ditambah | HUD tunggal, CardLayout scene, tombol/dpad dan status aksi. |
| `src/main/java/com/innoelektrica/polymorphism/view/HomePanel.java` | Ditambah | Rumah, garage, pilihan kendaraan dan konteks ending. |
| `src/main/java/com/innoelektrica/polymorphism/view/InputController.java` | Ditambah | Key bindings jendela, normalisasi input, stop dan release dari dialog modal. |
| `src/main/java/com/innoelektrica/polymorphism/view/MainFrame.java` | Diubah | Satu Timer, menu/dialog, save/load, pause, fokus, restore demo dan lifecycle audio. |
| `src/main/java/com/innoelektrica/polymorphism/view/MenuPanel.java` | Ditambah | Mulai Baru, Muat, Lanjutkan, Petunjuk, Demo dan Keluar. |
| `src/main/java/com/innoelektrica/polymorphism/view/ParkPanel.java` | Diubah | Karakter berjalan, toko, lima hewan, friendship, reaksi dan parkir. |
| `src/main/java/com/innoelektrica/polymorphism/view/ScenePanel.java` | Ditambah | Canvas 1000×600 adaptif, mouse mapping, pencahayaan dan helper render. |
| `src/main/java/com/innoelektrica/polymorphism/view/UiActions.java` | Ditambah | Kontrak penghubung aksi scene/HUD dengan frame. |
| `src/main/java/com/innoelektrica/polymorphism/view/UiTheme.java` | Ditambah | Warna, tombol, paragraf dengan jumlah baris yang menyesuaikan isi. |
| `src/main/java/com/innoelektrica/polymorphism/view/WorldMapPanel.java` | Diubah | Peta jalan yang cocok dengan collision, seluruh pintu dan kendaraan aktif. |
| `src/test/java/com/innoelektrica/polymorphism/game/GameTests.java` | Ditambah | 233 pemeriksaan logika, resource JAR, input, demo dan render headless. |
| `src/test/java/com/innoelektrica/polymorphism/game/PersistenceProbe.java` | Ditambah | Tulis dan load state di dua JVM independen. |
| `test.bat` | Ditambah | Build dan uji regresi Windows. |
| `tools/Build.java` | Ditambah | Build offline bersih memakai compiler JDK 17; resource dan test optional. |
| `tools/JarBuilder.java` | Diubah | JAR executable melalui file sementara, validasi dan penggantian target. |

## Penggantian dan dokumentasi tambahan

- `view/MapNavigationListener.java` lama digantikan kontrak `UiActions` dan pengelola state bersama, sehingga panel tidak menyimpan saldo/BBM sendiri.
- `docs/PANDUAN_DEMO.md`, `PENJELASAN_PBO.md`, `DAFTAR_PERUBAHAN.md`, `HASIL_PENGUJIAN.md`, `BUILD_TEST_LOG.txt` dan `screenshots/*.png` ditambahkan untuk presentasi serta bukti pengujian.
- `dist/ADayInMyCity.jar` dibangun ulang dari seluruh source final dengan bytecode Java 17. JAR lama dan output `.class` lama tidak dipakai.
- `out/` dan `target/` tidak dikemas sebagai source; build menghasilkan ulang `.class`, tools dan test output. JAR final sudah berisi kelas yang baru dikompilasi.
- `sources.txt` lama tidak diperlukan karena build menemukan seluruh Java source secara otomatis.
- **Semua 16 aset asli (11 PNG + 5 WAV) identik byte dengan ZIP awal.** Peta jalan, karakter, toko/garage tambahan dan elemen GUI digambar dengan Java2D; tidak ada resource tambahan yang hilang.
