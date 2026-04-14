# Holiday Messenger (FestiveMagic)

Android app for scheduling and sending holiday/birthday greeting messages via SMS and WhatsApp.

## Repository

- **GitHub**: https://github.com/Randomtom83/Holiday-messenger-
- **Owner**: randomtom83 (thomas.reynolds.aia@gmail.com)

## On Startup — Holiday Discovery

Every time a conversation opens in this project, Claude should:

1. **Search for new/upcoming holidays** that are not already in the app's holiday list (`HolidayCalendar.kt` and `HolidayRepository.seedDefaultHolidays()`). Consider:
   - Regional US holidays (Presidents' Day, MLK Day, Veterans Day, Memorial Day, Labor Day, etc.)
   - Cultural/awareness days gaining popularity (Juneteenth already included, but e.g., International Women's Day, Earth Day, etc.)
   - Any holidays in the next 60 days not yet covered
2. **Report findings** to the user — suggest which holidays to add and draft potential message templates.
3. Do NOT auto-modify code for holiday additions without user approval.

## Build & Deploy

When building the app:

1. Run `./gradlew assembleDebug` or `./gradlew assembleRelease`
2. **Push APK to connected phone**: `adb install -r app/build/outputs/apk/debug/FestiveMagic-1.0.0.apk`
3. **Copy build to ReynoldsFamily location**: Copy the APK to `Y:\ReynoldsFamily\builds\`
   ```bash
   cp app/build/outputs/apk/debug/FestiveMagic-*.apk /y/ReynoldsFamily/builds/
   ```
   For release builds:
   ```bash
   cp app/build/outputs/apk/release/FestiveMagic-*.apk /y/ReynoldsFamily/builds/
   ```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **DI**: Hilt
- **Database**: Room (version 3, with migrations)
- **Scheduling**: WorkManager (daily scheduler at 5 AM, weekly birthday sync)
- **AI**: Google Gemini API (for message generation)
- **Min SDK**: 26 / **Target SDK**: 35 / **Java**: 17

## Architecture

- Clean Architecture: Entity -> DAO -> Repository -> ViewModel -> Compose UI
- 18 seeded holidays with variable-date support (Easter, Eid, Diwali, etc.)
- Message templates use `{name}` placeholder resolved at send time
- SMS sent directly; WhatsApp via notification (user-initiated)

## Key Paths

- Entities: `app/src/main/java/com/holidaymessenger/data/db/entity/`
- Workers: `app/src/main/java/com/holidaymessenger/worker/`
- Holiday calendar logic: `app/src/main/java/com/holidaymessenger/util/HolidayCalendar.kt`
- Holiday seeding: `app/src/main/java/com/holidaymessenger/data/repository/HolidayRepository.kt`
