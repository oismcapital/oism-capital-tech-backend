---
inclusion: auto
---

# OISM Capital Tech — Engineering Guidelines (Backend)

## Language Standard
- **All code must be written in English**: variable names, method names, class names, comments, log messages, column names, enum values, and DTO fields.
- Exception: user-facing error messages returned in API responses may be in Portuguese.

## Financial Safety
- Use **`BigDecimal`** for all monetary calculations. Never use `double` or `float` for financial values.
- Rounding must use `RoundingMode.HALF_UP`.

## Resilience — Interest Processing
- Daily interest calculation must run via **Scheduled Task (robot/cron)**.
- Each execution must generate **audit logs** recording: processing date, plan processed, calculated value, and result.

## Concurrency
- Apply **optimistic or pessimistic locks** on the database for withdrawal and redemption operations.
- Prevent multiple simultaneous requests from consuming the same balance.

## Architecture — Service Pattern
- Isolate **interest calculation logic** from persistence logic.
- Expected structure:
  - `Controller` → receives request
  - `Service` → contains business rules and calculations
  - `Repository` → data persistence
- Accrual logic (daily interest calculation) must reside in a dedicated service (e.g., `InvestmentService`).

## Atomic Operations
- Plan purchase (Wallet debit + Investment creation) must run within a **single transaction** (`@Transactional`).
- Plan maturity (Wallet credit + Investment status update) must also be transactional.

## Types and Validations
- Dates: use `LocalDate` / `LocalDateTime` with defined timezone.
- Plan status: use `enum` (e.g., `ACTIVE`, `MATURED`, `WITHDRAWN`).
- Validate available balance before any Wallet debit operation.
