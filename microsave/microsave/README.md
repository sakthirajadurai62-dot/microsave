# MicroSave - Self-Help Group Savings Tracker
Java 17, Spring Boot 3.2, MySQL 8.

## Run
1. Start MySQL. Edit username/password in src/main/resources/application.properties.
2. mvn spring-boot:run  (database `microsave` and tables are auto-created)
3. Swagger UI: http://localhost:8080/swagger-ui.html

## Demo flow
1. POST /api/groups          {"name":"Mahalir SHG","location":"Coimbatore"}
2. POST /api/members         {"name":"Lakshmi","phone":"9876543210","groupId":1}
   POST /api/members         {"name":"Meena","phone":"9876543211","groupId":1}
3. POST /api/contributions   {"memberId":1,"amount":500}   (repeat a few times, also memberId 2)
4. GET  /api/groups/1/pool
5. POST /api/loans           {"memberId":1,"amount":800}   -> 201
6. POST /api/loans           {"memberId":1,"amount":100}   -> 422 already has active loan
7. POST /api/loans           {"memberId":2,"amount":99999} -> 422 exceeds pool
8. POST /api/repayments      {"loanId":1,"amount":300}
9. GET  /api/members/1/summary
10. POST /api/repayments     {"loanId":1,"amount":500}     -> loan CLOSED
