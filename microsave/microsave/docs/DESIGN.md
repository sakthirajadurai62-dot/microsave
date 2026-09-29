# MicroSave – Design Documents

## 1. Use case diagram
```mermaid
flowchart LR
  A([Group Treasurer / Admin])
  A --> U1(Manage Groups)
  A --> U2(Manage Members)
  A --> U3(Record weekly contribution)
  A --> U4(Disburse internal loan)
  A --> U5(Log loan repayment)
  A --> U6(View member savings and outstanding loan)
  A --> U7(View group pool)
  U4 -.includes.-> V1(Check active loan rule)
  U4 -.includes.-> V2(Check pool balance rule)
  U5 -.includes.-> V3(Check loan active and amount within outstanding)
```

## 2. Class diagram
```mermaid
classDiagram
  class SelfHelpGroup { Long id; String name; String location; LocalDate createdDate }
  class Member { Long id; String name; String phone; boolean active; LocalDate joinedDate }
  class Contribution { Long id; BigDecimal amount; LocalDate contributionDate }
  class Loan { Long id; BigDecimal principal; BigDecimal outstandingBalance; LocalDate disbursedDate; LoanStatus status }
  class Repayment { Long id; BigDecimal amount; LocalDate repaymentDate }
  class LoanStatus { <<enumeration>> ACTIVE; CLOSED }
  SelfHelpGroup "1" o-- "*" Member : has
  Member "1" --> "*" Contribution : makes
  Member "1" --> "*" Loan : borrows
  Loan "1" --> "*" Repayment : repaid by
  Loan --> LoanStatus
```

## 3. ER / schema diagram
```mermaid
erDiagram
  SHG_GROUP ||--o{ MEMBER : has
  MEMBER ||--o{ CONTRIBUTION : makes
  MEMBER ||--o{ LOAN : borrows
  LOAN ||--o{ REPAYMENT : "repaid by"
  SHG_GROUP { bigint id PK  varchar name UK  varchar location  date created_date }
  MEMBER { bigint id PK  varchar name  varchar phone UK  bit active  date joined_date  bigint group_id FK }
  CONTRIBUTION { bigint id PK  decimal amount  date contribution_date  bigint member_id FK }
  LOAN { bigint id PK  decimal principal  decimal outstanding_balance  date disbursed_date  varchar status  bigint member_id FK }
  REPAYMENT { bigint id PK  decimal amount  date repayment_date  bigint loan_id FK }
```

## 4. Layered architecture
```mermaid
flowchart TB
  C[Client: Postman / Swagger] --> CT[Controller layer - REST, Valid]
  CT --> SV[Service layer - business rules, Transactional]
  SV --> RP[Repository layer - Spring Data JPA]
  RP --> DB[(MySQL)]
  EX[GlobalExceptionHandler - RestControllerAdvice] -.handles errors from.-> CT
```

## 5. Sequence: disburse loan
```mermaid
sequenceDiagram
  Client->>LoanController: POST /api/loans
  LoanController->>LoanService: disburse(req)
  LoanService->>MemberRepository: findById
  LoanService->>LoanRepository: existsByMemberIdAndStatus(ACTIVE)
  LoanService->>GroupRepository: findByIdForUpdate (lock)
  LoanService->>ContributionRepository: sumByGroup
  LoanService->>LoanRepository: sumOutstandingByGroup
  LoanService->>LoanRepository: save(loan)
  LoanService-->>Client: 201 LoanResponse (or 422 if a rule fails)
```

## 6. Business rules
| Rule | Where enforced |
|---|---|
| Pool = total contributions - outstanding loans | GroupService.getPool, LoanService.disburse |
| Loan amount <= pool | LoanService.disburse (before save) |
| One active loan per member | LoanService.disburse (before save) |
| Repayment only on ACTIVE loan, <= outstanding | RepaymentService.repay |
| Loan auto-CLOSED at zero balance | RepaymentService.repay |
| Only active members can contribute or borrow | ContributionService / LoanService |
