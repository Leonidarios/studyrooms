StudyRooms
===

Θέμα 2 - Κατανεμημένα Συστήματα, Λεωνίδας Μαρκάκης (it21650)

Βασισμένο στο [DS-Lab-OfficeHours](https://github.com/gkoulis/DS-Lab-OfficeHours), χρησιμοποιεί την υπηρεσία [DS-Lab-NOC](https://github.com/gkoulis/DS-Lab-NOC).

Βίντεο: <https://youtu.be/xH_UzCHSXpM>

---

Run NOC first (port 8081), with empty `routee.app-id` / `routee.app-secret` in its `application.yaml`:

```shell
cd DS-Lab-NOC
./mvnw spring-boot:run  # MacOS / Linux
./mvnw.cmd spring-boot:run  # Windows
```

---

Run StudyRooms (port 8080):

```shell
cd studyrooms
./mvnw spring-boot:run  # MacOS / Linux
./mvnw.cmd spring-boot:run  # Windows
```

---

Open in browser: [localhost:8080](http://localhost:8080)

Users (password `1234`): `it2023001@hua.gr`, `it2023002@hua.gr` (students), `s0001@hua.gr` (staff)

---

API: [localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Token: `POST /api/v1/auth/client-tokens` with `client01` / `s3cr3t` (read + write) or `client02` / `s3cr3t` (read only)

---