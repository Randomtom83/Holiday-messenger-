# Holiday Messenger — Weekly Update (2026-04-20)

> **Note on current state:** The codebase seeds **9 holidays** (not 18). `HolidayCalendar.kt`
> and `HolidayRepository.seedDefaultHolidays()` contain: New Year's Day, Valentine's Day, Easter,
> Mother's Day, Father's Day, Independence Day, Halloween, Thanksgiving, Christmas.
> The CLAUDE.md lists 18 aspirational holidays; none of the additional 9 (Lunar New Year, Passover,
> Juneteenth, Eid al-Fitr, Eid al-Adha, Rosh Hashanah, Election Day, Diwali, Hanukkah) are
> currently wired into either file.

---

## New Holiday Discoveries

Holidays **not** in the codebase, ordered by date. Includes all in the next 90 days (through 2026-07-19) plus other high-value additions.

---

### 🌍 Earth Day — April 22 (fixed, `04-22`) ⚡ 2 days away!

| | |
|---|---|
| **Type** | Fixed |
| **monthDay** | `04-22` |
| **Next date** | 2026-04-22 |

**Message templates:**
1. "Happy Earth Day, {name}! Hope today reminds us all how worth protecting this beautiful planet is."
2. "{name}, wishing you a wonderful Earth Day — even small acts of care add up to something great."
3. "Earth Day greetings, {name}! Here's to cleaner skies, greener spaces, and a healthier world for everyone."

---

### 🍀 Cinco de Mayo — May 5 (fixed, `05-05`)

| | |
|---|---|
| **Type** | Fixed |
| **monthDay** | `05-05` |
| **Next date** | 2026-05-05 |

**Message templates:**
1. "¡Feliz Cinco de Mayo, {name}! Hope your day is full of good food, great music, and even better company."
2. "Happy Cinco de Mayo, {name}! Wishing you a festive and fun celebration today."
3. "{name}, hope Cinco de Mayo brings you all the tacos and good vibes you deserve!"

---

### 🪖 Memorial Day — Last Monday of May (variable)

| | |
|---|---|
| **Type** | Variable — last Monday of May |
| **Next date** | 2026-05-25 |
| **Rule** | `nthDayOfWeekInMonth(year, Month.MAY, DayOfWeek.MONDAY, 5)` or `lastInMonth(MONDAY)` |

**Message templates:**
1. "Wishing you a meaningful Memorial Day, {name}. Grateful for those who served and sacrificed."
2. "Happy Memorial Day, {name}! Hope you get to enjoy this day with the people you love while honoring those who made it possible."
3. "{name}, pausing today to remember those who gave everything. Hope you have a peaceful Memorial Day."

---

### ✊ Juneteenth — June 19 (fixed, `06-19`)

| | |
|---|---|
| **Type** | Fixed |
| **monthDay** | `06-19` |
| **Next date** | 2026-06-19 |

**Message templates:**
1. "Happy Juneteenth, {name}! A day to honor freedom, resilience, and the power of community."
2. "{name}, wishing you a joyful Juneteenth — celebrating freedom, culture, and the ongoing journey toward justice."
3. "Happy Juneteenth, {name}! May today be a reminder of how far we've come and inspiration for how far we'll go."

---

### 🏛️ MLK Day — 3rd Monday of January (variable)

| | |
|---|---|
| **Type** | Variable — 3rd Monday of January |
| **Next date** | 2027-01-18 |
| **Rule** | `nthDayOfWeekInMonth(year, Month.JANUARY, DayOfWeek.MONDAY, 3)` |

**Message templates:**
1. "Happy MLK Day, {name}. May Dr. King's dream of justice and equality continue to inspire us all."
2. "{name}, wishing you a reflective and hopeful MLK Day — his words still ring true today."
3. "On this MLK Day, {name}, I'm reminded that love is always stronger than hate. Wishing you a meaningful day."

---

### 💜 International Women's Day — March 8 (fixed, `03-08`)

| | |
|---|---|
| **Type** | Fixed |
| **monthDay** | `03-08` |
| **Next date** | 2027-03-08 |

**Message templates:**
1. "Happy International Women's Day, {name}! Celebrating the incredible strength and impact of women everywhere — including you."
2. "{name}, today and every day — thank you for being the amazing woman you are. Happy International Women's Day!"
3. "Wishing you a wonderful International Women's Day, {name}! The world is better because of women like you."

---

### 🍀 St. Patrick's Day — March 17 (fixed, `03-17`)

| | |
|---|---|
| **Type** | Fixed |
| **monthDay** | `03-17` |
| **Next date** | 2027-03-17 |

**Message templates:**
1. "Happy St. Patrick's Day, {name}! May luck be on your side and your day be full of green and good cheer."
2. "{name}, wishing you all the luck of the Irish today! Happy St. Paddy's Day!"
3. "Hope your St. Patrick's Day is full of fun, laughter, and maybe a little magic, {name}!"

---

### 🏛️ Presidents' Day — 3rd Monday of February (variable)

| | |
|---|---|
| **Type** | Variable — 3rd Monday of February |
| **Next date** | 2027-02-15 |
| **Rule** | `nthDayOfWeekInMonth(year, Month.FEBRUARY, DayOfWeek.MONDAY, 3)` |

**Message templates:**
1. "Happy Presidents' Day, {name}! Hope you're enjoying a well-deserved day off."
2. "{name}, wishing you a relaxing Presidents' Day — a great excuse to slow down and recharge."
3. "Happy Presidents' Day, {name}! Hope the long weekend is treating you well."

---

### 🛠️ Labor Day — 1st Monday of September (variable)

| | |
|---|---|
| **Type** | Variable — 1st Monday of September |
| **Next date** | 2026-09-07 |
| **Rule** | `nthDayOfWeekInMonth(year, Month.SEPTEMBER, DayOfWeek.MONDAY, 1)` |

**Message templates:**
1. "Happy Labor Day, {name}! Here's to hard work, well-earned rest, and the people who keep everything running."
2. "{name}, hope you're kicking back and relaxing this Labor Day — you've earned it!"
3. "Wishing you a wonderful Labor Day weekend, {name}! Enjoy every moment of it."

---

### 🪶 Indigenous Peoples' Day — 2nd Monday of October (variable)

| | |
|---|---|
| **Type** | Variable — 2nd Monday of October |
| **Next date** | 2026-10-12 |
| **Rule** | `nthDayOfWeekInMonth(year, Month.OCTOBER, DayOfWeek.MONDAY, 2)` |

**Message templates:**
1. "Happy Indigenous Peoples' Day, {name}. A day to honor, celebrate, and learn from the rich cultures that shaped this land."
2. "{name}, wishing you a meaningful Indigenous Peoples' Day — honoring resilience, culture, and community."
3. "Happy Indigenous Peoples' Day, {name}! May we always carry forward the stories and wisdom of Indigenous peoples."

---

### 🎖️ Veterans Day — November 11 (fixed, `11-11`)

| | |
|---|---|
| **Type** | Fixed |
| **monthDay** | `11-11` |
| **Next date** | 2026-11-11 |

**Message templates:**
1. "Happy Veterans Day, {name}. Thank you for your service — your sacrifice means more than words can say."
2. "{name}, wishing you a meaningful Veterans Day and sending heartfelt gratitude for all who have served."
3. "On Veterans Day, {name}, I'm thinking of all who wore the uniform with honor. Thank you and everyone who served."

---

### 🕯️ Kwanzaa — December 26 (fixed, `12-26`)

| | |
|---|---|
| **Type** | Fixed |
| **monthDay** | `12-26` |
| **Next date** | 2026-12-26 |

**Message templates:**
1. "Happy Kwanzaa, {name}! Wishing you a beautiful celebration of unity, culture, and community."
2. "{name}, may this Kwanzaa season bring you joy, reflection, and connection with the people you love most."
3. "Happy Kwanzaa, {name}! Celebrating the rich heritage and values that make our communities strong."

---

### 🧠 World Mental Health Day — October 10 (fixed, `10-10`)

| | |
|---|---|
| **Type** | Fixed |
| **monthDay** | `10-10` |
| **Next date** | 2026-10-10 |

**Message templates:**
1. "Happy World Mental Health Day, {name}. Just a reminder: your feelings matter, and so do you."
2. "{name}, checking in on this World Mental Health Day — hope you're taking good care of yourself. I'm always here if you need anything."
3. "On World Mental Health Day, {name}, sending you love and a reminder that it's okay to not be okay. You've got people who care."

---

## Fresh Message Templates

Three new options per existing holiday (different from the current defaults in code).

---

### 🎆 New Year's Day (01-01)
1. "{name}, here's to a fresh start and all the adventures waiting for you this year!"
2. "Cheers to you, {name}! May this year bring you everything you've been hoping for and more."
3. "Another year, another chance to make it your best one yet. Happy New Year, {name}!"

---

### 💕 Valentine's Day (02-14)
1. "Thinking of you today, {name} — you make the world a little sweeter just by being in it. Happy Valentine's Day!"
2. "Happy Valentine's Day, {name}! Love you to bits — hope your day is full of all good things."
3. "Whether it's chocolate, flowers, or a cozy night in, hope your Valentine's Day is exactly what you want, {name}!"

---

### 🐣 Easter (variable)
1. "Wishing you a bright and joyful Easter, {name} — may this season bring you hope and renewal."
2. "Happy Easter, {name}! Hope your day is full of good food, great company, and maybe a little chocolate."
3. "{name}, wishing you all the peace and joy this beautiful Easter season has to offer."

---

### 🌹 Mother's Day (2nd Sunday of May)
1. "Happy Mother's Day, {name}! The love and strength you bring to everyone around you is truly something special."
2. "Here's to you, {name} — a mother who makes every day brighter. Hope today is all about you!"
3. "{name}, there's no one quite like you. Happy Mother's Day to one of the very best!"

---

### 👔 Father's Day (3rd Sunday of June)
1. "Happy Father's Day, {name}! Hope you get to relax, enjoy your favorite things, and be celebrated the way you deserve."
2. "To the dad who always shows up — Happy Father's Day, {name}! Today is all yours."
3. "{name}, wishing you a Father's Day as great as you are. Enjoy every single moment!"

---

### 🎇 Independence Day (07-04)
1. "Happy 4th of July, {name}! Hope your day is full of sunshine, good food, and fireworks."
2. "Wishing you a festive and fun Independence Day, {name}! Enjoy every bit of the celebration."
3. "{name}, here's to freedom, family, and a fantastic Fourth of July — stay safe out there!"

---

### 🎃 Halloween (10-31)
1. "Hope your Halloween is full of tricks, treats, and just the right amount of scare, {name}!"
2. "Happy Halloween, {name}! Whatever costume you're in, I hope tonight is an absolute blast."
3. "Wishing you a wickedly fun Halloween, {name} — may your candy bowl overflow!"

---

### 🦃 Thanksgiving (4th Thursday of November)
1. "Happy Thanksgiving, {name}! Wishing you a day full of warmth, great food, and even better company."
2. "{name}, so thankful to have you in my corner. Hope your Thanksgiving is absolutely wonderful."
3. "Gratitude looks good on everyone, especially today. Happy Thanksgiving, {name}!"

---

### 🎄 Christmas (12-25)
1. "Merry Christmas, {name}! Hope your holiday is wrapped in warmth and filled with all the things you love most."
2. "Wishing you and yours a peaceful, joyful Christmas, {name}. So glad you're part of my world!"
3. "{name}, from my heart to yours — Merry Christmas! May this season bring you nothing but good things."

---

## Recommendations

Top 5 new holidays most worth adding to the app, ranked by urgency and cultural reach:

### 1. 🌍 Earth Day (04-22) — **Add immediately**
- Federal recognition, huge cultural moment, growing messaging trend
- Just 2 days away — great time to wire it in
- Fixed date makes implementation trivial

### 2. ✊ Juneteenth (06-19) — **Add next sprint**
- Federal holiday since 2021, listed in CLAUDE.md as already "included" but absent from code
- Fixed date, significant cultural weight
- Coming up June 19 — 60 days out

### 3. 🪖 Memorial Day (variable, late May) — **Add next sprint**
- One of the most universally recognized US federal holidays
- Widely sent greetings; strong use case for the app
- Coming up May 25 — only 35 days away

### 4. 🛠️ Labor Day (variable, early September) — **Add in next batch**
- Federal holiday, huge reach across all demographics
- Natural end-of-summer touchpoint for greetings

### 5. 🏛️ MLK Day (variable, January) — **Add in next batch**
- Federal holiday with deep cultural and historical significance
- Part of the original aspirational 18-holiday list
- Variable date — needs `nthDayOfWeekInMonth` logic already in `HolidayCalendar.kt`

---

*Generated by Holiday Messenger maintenance agent — 2026-04-20*
