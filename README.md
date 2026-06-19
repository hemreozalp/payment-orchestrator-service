# Payment Orchestrator (Eşzamanlılık ve Hata Toleranslı Ödeme Yönetimi)

Bu proje; birden fazla ödeme sağlayıcısının yönetimini, yüksek eşzamanlı istek (concurrency) altındaki veri tutarlılığını ve hata anındaki telafi mekanizmalarını ele alan bir backend simülasyonudur. Projenin amacı, dağıtık sistemlerde karşılaşılan yarış durumları (race conditions) ve veri tutarlılığı (consistency) gibi temel mühendislik problemlerine pratik çözümler üretmektir.

---

## Proje Kapsamında Kazanılan Yetkinlikler ve Uygulamalar

### 1. Esnek Sağlayıcı Yönetimi (Factory Tasarım Kalıbı)
* Sistemin farklı ödeme sağlayıcıları (Iyzico, Stripe vb.) ile entegre olabileceği öngörülerek, iş mantığı ile entegrasyon katmanları Factory Pattern kullanılarak ayrıştırılmıştır.
* Yeni bir sağlayıcı eklendiğinde mevcut kod tabanına dokunulmadan (Open-Closed Principle) sistemin genişletilebilmesi sağlanmıştır.

### 2. Akıllı Yeniden Deneme ve Hata Yönetimi (Failover & Exponential Backoff)
* Para birimi kurallarına göre dinamik banka önceliklendirmesi yapılmıştır.
* Birincil sağlayıcının hata dönmesi durumunda, işlemin kesintiye uğramaması için otomatik olarak yedek sağlayıcıya geçiş (Failover) mekanizması kurulmuştur.
* Sağlayıcı sunucularını ardışık isteklerle yük altında bırakmamak adına, başarısız denemeler arasına katlanarak artan zaman gecikmeleri koyan Exponential Backoff algoritması uygulanmıştır.

### 3. Yarış Durumları ve Dağıtık Kilit Yönetimi (Redis Distributed Lock)
* Aynı benzersiz isteğin (Idempotency Key) milisaniyeler içinde peş peşe tetiklenmesi senaryosunda (Race Condition), mükerrer tahsilat yapılmasını engellemek adına Redis SETNX komutu ile kilit yönetimi kurgulanmıştır.
* Bu sayede uygulamanın birden fazla örneğinin (instance) çalıştığı senaryolarda thread senkronizasyonu güvence altına alınmıştır.

### 4. Gelişmiş Benzersizlik Güvencesi (Pure Idempotency)
* Çakışan mükerrer isteklerde, ikinci isteği doğrudan hata fırlatarak reddetmek yerine, sistem Polling (Spin Lock) mantığı ile kilit süresince güvenli bir şekilde ayakta bekletilmiştir.
* İlk istek veritabanı kaydını tamamladığı an, bekleyen ikinci istek veritabanındaki güncel durumu okuyarak istemciye hatasız bir şekilde orijinal yanıtı dönmüştür. Böylece mükerrer çağrılar veri bütünlüğü bozulmadan absorbe edilmiştir.

### 5. Asenkron Telafi Mekanizması (Saga Compensation Felsefesi)
* Tüm yedek sağlayıcıların da başarısız olduğu uç senaryolarda, sistem genelindeki veri bütünlüğünü korumak adına Saga Pattern'in compensation (telafi) felsefesi uygulanmıştır.
* Ana thread bloke edilmeden, RabbitMQ üzerinden asenkron bir başarısızlık eventi fırlatılmış ve bu eventi tüketen consumer vasıtasıyla ilişkili siparişlerin iptali veya stokların geri bırakılması gibi ters işlem simülasyonları gerçekleştirilmiştir.

### 6. Mesaj Kuyruğu Yönetimi (RabbitMQ AMQP)
* Başarılı bildirimler ve başarısız telafi süreçleri için Direct Exchange yapısı kullanılarak net yönlendirme (Routing Key) kuralları tanımlanmıştır.
* Verilerin AMQP protokolü üzerinde güvenle taşınması amacıyla JacksonJsonMessageConverter konfigürasyonu gerçekleştirilmiştir.

---

## Kullanılan Teknolojiler

* **Backend Framework:** Spring Boot 3.x, Spring Data JPA
* **Database / ORM:** PostgreSQL / Hibernate
* **Caching & Lock Management:** Redis (StringRedisTemplate)
* **Message Broker:** RabbitMQ
