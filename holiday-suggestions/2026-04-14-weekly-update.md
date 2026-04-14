# Holiday Messenger — Weekly Update: 2026-04-14

## Codebase Audit Note

The prompt listed 18 seeded holidays, but the actual code contains **9 holidays**.
The following holidays mentioned in the prompt do NOT appear in `HolidayRepository.seedDefaultHolidays()`
or `HolidayCalendar.kt` and should be treated as candidates to add:

- Lunar New Year, Passover, Juneteenth, Eid al-Fitr, Eid al-Adha,
  Rosh Hashanah, Election Day, Diwali, Hanukkah

These are flagged again in the Recommendations section below.

---

## New Holiday Discoveries

Holidays in the next 90 days (2026-04-14 → 2026-07-13) **not in the current 9 seeded holidays**:

---

### Earth Day — April 22 (fixed, `04-22`)

**Templates:**
1. `Happy Earth Day, {name}! Thanks for being someone who cares about this beautiful planet we share.`
2. `{name}, hope Earth Day reminds you just how amazing this world is — and how worth protecting.`
3. `Happy Earth Day, {name}! Small actions, big impact. Together we've got this.`

---

### Arbor Day — last Friday of April (variable; 2026: April 24)

**Templates:**
1. `Happy Arbor Day, {name}! Go plant something — the future will thank you.`
2. `{name}, wishing you a rooted and grounded Arbor Day. One tree at a time!`
3. `Happy Arbor Day, {name}! Here's to all the shade, oxygen, and beauty trees bring our world.`

---

### Cinco de Mayo — May 5 (fixed, `05-05`)

**Templates:**
1. `Feliz Cinco de Mayo, {name}! Hope you're celebrating with good food and even better company.`
2. `Happy Cinco de Mayo, {name}! A great excuse to enjoy some guacamole and good vibes.`
3. `{name}, Cinco de Mayo is here — hope your day is as festive as you are!`

---

### Armed Forces Day — 3rd Saturday of May (variable; 2026: May 16)

**Templates:**
1. `Happy Armed Forces Day, {name}! Thank you for your service and sacrifice.`
2. `Thinking of you this Armed Forces Day, {name} — your dedication means everything.`
3. `On Armed Forces Day, {name}, we honor your courage and commitment. Thank you.`

---

### Memorial Day — last Monday of May (variable; 2026: May 25)

**Templates:**
1. `Happy Memorial Day, {name}! Take a moment to honor those who gave everything, then enjoy the day.`
2. `Thinking of you this Memorial Day, {name}. Grateful for the freedoms we share.`
3. `Hope you have a restful Memorial Day weekend, {name}! Wishing you sun, good food, and great memories.`

---

### Flag Day — June 14 (fixed, `06-14`)

**Templates:**
1. `Happy Flag Day, {name}! A day to remember what that flag stands for — freedom, unity, and hope.`
2. `{name}, wishing you a proud Flag Day. The stars and stripes fly a little brighter because of people like you.`
3. `Happy Flag Day, {name}! Take a moment to appreciate all the good this country has to offer.`

---

### Juneteenth — June 19 (fixed, `06-19`)

> Note: Listed as existing in the CLAUDE.md prompt but **not present in the current code**. High-priority add.

**Templates:**
1. `Happy Juneteenth, {name}! A powerful reminder of freedom's ongoing journey — today we celebrate and remember.`
2. `{name}, Juneteenth is a day of joy, reflection, and gratitude. Wishing you a meaningful celebration.`
3. `Happy Juneteenth, {name}! Honoring the past and celebrating the freedom and resilience that carries us forward.`

---

### Notable holidays outside the 90-day window (also missing from the code)

These were listed in CLAUDE.md as "already included" but are not in the codebase — adding them is recommended:

| Holiday | Date Rule | Fixed monthDay |
|---|---|---|
| Lunar New Year | variable (Jan/Feb) | — |
| St. Patrick's Day | fixed | `03-17` |
| International Women's Day | fixed | `03-08` |
| Passover | variable (March/April) | — |
| Eid al-Fitr | variable (Islamic calendar) | — |
| Labor Day | 1st Monday of September | — |
| Indigenous Peoples' Day | 2nd Monday of October | — |
| Eid al-Adha | variable (Islamic calendar) | — |
| Veterans Day | fixed | `11-11` |
| Rosh Hashanah | variable (Sept/Oct) | — |
| Diwali | variable (Oct/Nov) | — |
| Election Day | 1st Tuesday after 1st Monday of November | — |
| Kwanzaa | fixed (start) | `12-26` |
| Hanukkah | variable (Nov/Dec) | — |
| New Year's Eve | fixed | `12-31` |
| MLK Day | 3rd Monday of January | — |
| Presidents' Day | 3rd Monday of February | — |

---

## Fresh Message Templates

Three new options per existing seeded holiday.

---

### New Year's Day (01-01)
1. `Cheers to a brand new chapter, {name}! May this year bring you everything you've been hoping for.`
2. `{name}, out with the old and in with the new — here's to a fantastic year ahead for you!`
3. `Happy New Year, {name}! May this year be your best one yet — you deserve it.`

---

### Valentine's Day (02-14)
1. `Hey {name}, thinking of you today and so grateful you're in my life. Happy Valentine's Day!`
2. `Happy Valentine's Day, {name}! Spreading a little love your way today.`
3. `{name}, whether you have a date or not — you are loved! Happy Valentine's Day.`

---

### Easter (variable)
1. `Wishing you a joyful Easter, {name}! Hope your day is bright and full of good things.`
2. `Happy Easter, {name}! May the season bring you renewal, warmth, and lots of chocolate.`
3. `Easter blessings to you and yours, {name}! Hope it's a beautiful day.`

---

### Mother's Day (2nd Sunday of May)
1. `Happy Mother's Day, {name}! The world is a better place because of moms like you.`
2. `Thinking of you today, {name}, and all the love and strength you pour into everything you do. Happy Mother's Day!`
3. `{name}, you make it look so effortless. Happy Mother's Day to one of the very best!`

---

### Father's Day (3rd Sunday of June)
1. `Happy Father's Day, {name}! Your steady presence means more than you know.`
2. `Cheers to you, {name}! Hope Father's Day treats you as well as you treat the people you love.`
3. `Happy Father's Day, {name}! Time to kick back and enjoy a day that's all yours.`

---

### Independence Day (07-04)
1. `Happy 4th, {name}! Hope your day is full of fireworks, good food, and great company.`
2. `{name}, wishing you a fantastic Independence Day — enjoy every moment of the long weekend!`
3. `Happy Independence Day, {name}! Freedom never tasted so good. Enjoy the festivities!`

---

### Halloween (10-31)
1. `Happy Halloween, {name}! Stay spooky and have a frighteningly good time tonight.`
2. `{name}, hope your Halloween is full of more treats than tricks! Enjoy the fun.`
3. `Wishing you a ghoulishly great Halloween, {name}! Don't eat all the candy at once.`

---

### Thanksgiving (4th Thursday of November)
1. `Happy Thanksgiving, {name}! You're at the top of my list of things I'm grateful for this year.`
2. `{name}, hoping your Thanksgiving table is full of food, laughter, and people you love.`
3. `Wishing you a warm and wonderful Thanksgiving, {name} — you deserve every bit of it.`

---

### Christmas (12-25)
1. `Wishing you a magical Christmas, {name}! May your holiday season be warm, bright, and full of joy.`
2. `Merry Christmas, {name}! Hope Santa was good to you and the day is everything you hoped for.`
3. `{name}, sending you big holiday hugs and warm wishes this Christmas. Enjoy every moment!`

---

## Recommendations

### Top 5 holidays most worth adding (priority order)

**1. Juneteenth — June 19 (fixed, `06-19`)**
Federal holiday since 2021. It was listed in CLAUDE.md as already included but is missing from the code.
This is a critical gap to close — it's both a legal federal holiday and culturally significant. Adding it is a
one-line fix in `seedDefaultHolidays()` plus a variable-date entry in `HolidayCalendar`.

**2. Memorial Day — last Monday of May (variable)**
One of the most universally observed US federal holidays. Falls in the next 90 days (May 25, 2026).
The app currently has no late-May holiday, leaving a gap in the spring/summer schedule.

**3. St. Patrick's Day — March 17 (fixed, `03-17`)**
Extremely widely celebrated across all backgrounds in the US. Fixed date, simple to add. Good for casual,
festive messaging and broadens the app's appeal for spring holidays.

**4. Labor Day — 1st Monday of September (variable)**
Federal holiday, end-of-summer observance. Listed in CLAUDE.md but missing from code. Creates a fall
bookend alongside Halloween and Thanksgiving.

**5. Veterans Day — November 11 (fixed, `11-11`)**
Federal holiday with deep personal meaning for many families. Fixed date, easy to add. Respectful and
meaningful tone distinguishes it from other fall holidays.

### Runners-up worth tracking
- **MLK Day** (3rd Monday of January) — federal holiday, important cultural recognition
- **International Women's Day** (March 8) — growing mainstream adoption, good for diverse contact lists
- **Diwali** (variable, Oct/Nov) — listed in CLAUDE.md as included but missing; significant cultural reach
- **Hanukkah** (variable, Nov/Dec) — same situation as Diwali
- **Kwanzaa** (Dec 26, fixed) — rounds out the December holiday coverage

---

*Generated by Holiday Messenger maintenance agent on 2026-04-14.*
