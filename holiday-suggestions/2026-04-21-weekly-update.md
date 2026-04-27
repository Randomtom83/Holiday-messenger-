# Holiday Messenger — Weekly Update (2026-04-21)

**Agent run date:** 2026-04-21  
**Next 90-day window:** 2026-04-21 → 2026-07-20  
**Codebase audit note:** `HolidayRepository.seedDefaultHolidays()` currently seeds **9 holidays**, not 18. The holidays listed as items 2–3 and 8–18 in the project brief (Lunar New Year, Passover, Juneteenth, Eid al-Fitr, Eid al-Adha, Rosh Hashanah, Election Day, Diwali, Hanukkah) are **absent from the code** and should be added.

---

## New Holiday Discoveries

Holidays **not** present in the current codebase, organized by date.

### Already-Planned But Missing From Code
These 9 holidays appear in the project brief but are not seeded — they should be added regardless.

| Holiday | Date Rule | monthDay / isVariable |
|---|---|---|
| Lunar New Year | Variable (late Jan – mid Feb) | `isVariable = true` |
| Passover | Variable (spring, Jewish calendar) | `isVariable = true` |
| Juneteenth | Fixed — June 19 | `monthDay = "06-19"` |
| Eid al-Fitr | Variable (Islamic calendar) | `isVariable = true` |
| Eid al-Adha | Variable (Islamic calendar) | `isVariable = true` |
| Rosh Hashanah | Variable (Jewish New Year, fall) | `isVariable = true` |
| Election Day | Variable (1st Tue after 1st Mon in Nov, even years) | `isVariable = true` |
| Diwali | Variable (Hindu lunisolar, Oct–Nov) | `isVariable = true` |
| Hanukkah | Variable (Jewish, Nov–Dec) | `isVariable = true` |

---

### New Discoveries — Next 90 Days

#### 1. Earth Day — April 22 (fixed)
`monthDay = "04-22"` · **Tomorrow!**

> Template A: "Happy Earth Day, {name}! Here's to protecting the beautiful planet we all share. 🌍"  
> Template B: "Earth Day greetings, {name}! Small actions add up — thanks for caring about our world."  
> Template C: "Hey {name}, happy Earth Day! Hope today reminds us all how worth protecting this place is."

---

#### 2. Cinco de Mayo — May 5 (fixed)
`monthDay = "05-05"`

> Template A: "Happy Cinco de Mayo, {name}! Wishing you good food, great company, and plenty of celebration today."  
> Template B: "¡Feliz Cinco de Mayo, {name}! Hope your day is full of color and joy."  
> Template C: "Happy Cinco de Mayo, {name}! A great excuse to celebrate with the people you love most."

---

#### 3. Memorial Day — Last Monday of May (variable)
`isVariable = true` · 2026 date: **May 25**

> Template A: "Happy Memorial Day, {name}. Today we pause to honor those who gave everything for our freedom. Thank you."  
> Template B: "Wishing you a meaningful Memorial Day, {name} — grateful for the heroes who made days like today possible."  
> Template C: "Happy Memorial Day, {name}! Hope you have a peaceful day to reflect, rest, and be with the people you love."

---

#### 4. Flag Day — June 14 (fixed)
`monthDay = "06-14"`

> Template A: "Happy Flag Day, {name}! Proud to share this country with you. 🇺🇸"  
> Template B: "Flag Day greetings, {name} — a little reminder of everything this flag stands for."  
> Template C: "Happy Flag Day, {name}! Hope it's a great day filled with pride and appreciation."

---

### Additional Notable Holidays (Outside 90-Day Window but Highly Relevant)

#### 5. Martin Luther King Jr. Day — 3rd Monday of January (variable)
`isVariable = true`

> Template A: "Happy MLK Day, {name}. His dream lives on in every act of kindness and justice. 🕊️"  
> Template B: "Thinking of you on MLK Day, {name}. May his legacy inspire us all to keep building a better world."  
> Template C: "Happy Martin Luther King Jr. Day, {name}! A day to remember, reflect, and recommit to the dream."

---

#### 6. St. Patrick's Day — March 17 (fixed)
`monthDay = "03-17"`

> Template A: "Happy St. Patrick's Day, {name}! May your day be filled with good luck and even better company. 🍀"  
> Template B: "Sending a little Irish luck your way, {name} — happy St. Patrick's Day!"  
> Template C: "Happy St. Patrick's Day, {name}! Hope the luck of the Irish finds you today and every day."

---

#### 7. International Women's Day — March 8 (fixed)
`monthDay = "03-08"`

> Template A: "Happy International Women's Day, {name}! Grateful every day for the incredible women in my life — including you."  
> Template B: "Thinking of you today, {name} — happy International Women's Day to an amazing person. 💜"  
> Template C: "Happy IWD, {name}! Here's to celebrating the strength, courage, and brilliance of women everywhere."

---

#### 8. Labor Day — 1st Monday of September (variable)
`isVariable = true`

> Template A: "Happy Labor Day, {name}! Hope you're getting a well-deserved break today. Enjoy every minute of it."  
> Template B: "Wishing you a relaxing Labor Day, {name} — you've earned it!"  
> Template C: "Happy Labor Day, {name}! Here's to honoring all the hard work you put in every single day."

---

#### 9. Veterans Day — November 11 (fixed)
`monthDay = "11-11"`

> Template A: "Happy Veterans Day, {name}. Thank you for your service and sacrifice — we are forever grateful."  
> Template B: "Thinking of you on Veterans Day, {name}. Your courage and dedication mean the world to all of us."  
> Template C: "Happy Veterans Day, {name}. Today and every day, we honor those who answered the call. 🎖️"

---

#### 10. Presidents' Day — 3rd Monday of February (variable)
`isVariable = true`

> Template A: "Happy Presidents' Day, {name}! Hope you're enjoying a well-earned day off."  
> Template B: "Happy Presidents' Day, {name} — a good day to appreciate the history that shaped this country."  
> Template C: "Wishing you a great Presidents' Day, {name}! Hope your Monday feels anything but ordinary."

---

#### 11. Kwanzaa — December 26–January 1 (fixed start)
`monthDay = "12-26"`

> Template A: "Happy Kwanzaa, {name}! Wishing you a beautiful celebration of unity, creativity, and purpose. 🕯️"  
> Template B: "Sending warm Kwanzaa greetings your way, {name} — may this season bring you joy and community."  
> Template C: "Happy Kwanzaa, {name}! Here's to honoring heritage, family, and the principles that guide us forward."

---

#### 12. Indigenous Peoples' Day — 2nd Monday of October (variable)
`isVariable = true`

> Template A: "Happy Indigenous Peoples' Day, {name}. Today we celebrate the rich cultures and enduring resilience of Native communities."  
> Template B: "Wishing you a meaningful Indigenous Peoples' Day, {name} — a day to honor and listen."  
> Template C: "Happy Indigenous Peoples' Day, {name}! Grateful for the wisdom and contributions of Indigenous peoples across this land."

---

## Fresh Message Templates

Three new options per existing holiday (different from the current codebase defaults).

### 1. New Year's Day (01-01)
> A: "Here's to a brand new chapter, {name}! Wishing you health, joy, and all your best year yet. 🥂"  
> B: "Happy New Year, {name}! May 2026 bring you everything you've been working toward and more."  
> C: "Cheers to new beginnings, {name}! So glad we're starting another year as part of each other's lives."

### 2. Lunar New Year (variable)
> A: "Happy Lunar New Year, {name}! Wishing you prosperity, good health, and a year full of happiness. 🧧"  
> B: "Lunar New Year greetings, {name}! May this year bring you abundance and joy with every new moon."  
> C: "Sending you warmest wishes this Lunar New Year, {name} — may fortune and happiness follow you all year long."

### 3. Valentine's Day (02-14)
> A: "Happy Valentine's Day, {name}! The world is a better place with you in it — truly."  
> B: "Thinking of you today, {name} — happy Valentine's Day from someone who's grateful you exist. ❤️"  
> C: "Hey {name}, happy Valentine's Day! Hope today reminds you just how loved you really are."

### 4. Easter (variable)
> A: "Happy Easter, {name}! Wishing you a joyful day filled with family, sunshine, and maybe a little chocolate. 🐣"  
> B: "Easter greetings, {name}! Hope this season brings you renewal, peace, and plenty of reasons to smile."  
> C: "Happy Easter, {name}! Sending you springtime warmth and all the happiness this day can hold."

### 5. Passover (variable)
> A: "Chag Pesach Sameach, {name}! Wishing you a meaningful Passover filled with family, story, and hope."  
> B: "Happy Passover, {name}! May your seder be filled with laughter, delicious food, and cherished company."  
> C: "Wishing you a peaceful and joyful Passover, {name} — may this season of freedom inspire you all year long."

### 6. Mother's Day (variable — 2nd Sunday of May)
> A: "Happy Mother's Day, {name}! The love and strength you bring to this world is extraordinary. 💐"  
> B: "Thinking of you today, {name} — wishing you a Mother's Day that's every bit as wonderful as you are."  
> C: "Happy Mother's Day, {name}! Hope today is full of rest, love, and the recognition you deserve every single day."

### 7. Father's Day (variable — 3rd Sunday of June)
> A: "Happy Father's Day, {name}! Your steady presence means more than words can say. Enjoy every minute today."  
> B: "Wishing you the most relaxing Father's Day, {name} — you've earned the day off a thousand times over."  
> C: "Happy Father's Day, {name}! Here's to celebrating the kind of dad who makes it look easy. 🎉"

### 8. Juneteenth (06-19)
> A: "Happy Juneteenth, {name}! Today we celebrate freedom, resilience, and the long road to justice for all."  
> B: "Wishing you a meaningful Juneteenth, {name} — a day to honor the past and recommit to a more just future."  
> C: "Happy Juneteenth, {name}! Today we remember, celebrate, and carry forward the legacy of freedom. ✊🏿"

### 9. Eid al-Fitr (variable)
> A: "Eid Mubarak, {name}! Wishing you and your family a joyful celebration filled with love and blessings. 🌙"  
> B: "Happy Eid al-Fitr, {name}! May this blessed day bring you peace, happiness, and cherished moments with loved ones."  
> C: "Eid Mubarak to you and yours, {name}! Wishing you a beautiful end to Ramadan and a joyful start to the celebrations."

### 10. Independence Day (07-04)
> A: "Happy 4th of July, {name}! Wishing you a day full of good food, great company, and plenty of fireworks. 🎆"  
> B: "Happy Independence Day, {name}! Hope your day is as bright and loud as a perfect summer evening."  
> C: "Celebrating freedom with you in spirit today, {name}! Happy 4th — hope it's an absolute blast."

### 11. Eid al-Adha (variable)
> A: "Eid al-Adha Mubarak, {name}! Wishing you and your family peace, joy, and a blessed celebration."  
> B: "Happy Eid al-Adha, {name}! May this day of sacrifice and gratitude bring you and your loved ones much happiness. 🌙"  
> C: "Eid Mubarak, {name}! Sending you warmth and blessings on this beautiful day of giving and reflection."

### 12. Rosh Hashanah (variable)
> A: "Shanah Tovah, {name}! Wishing you a sweet and meaningful New Year filled with health and joy. 🍎🍯"  
> B: "Happy Rosh Hashanah, {name}! May the year ahead bring you peace, prosperity, and all your heart desires."  
> C: "L'Shanah Tovah, {name} — wishing you and your family a beautiful and blessed Jewish New Year."

### 13. Halloween (10-31)
> A: "Happy Halloween, {name}! Hope your night is the perfect mix of spooky, fun, and sweet. 🎃"  
> B: "Boo! Happy Halloween, {name}! May your candy be plentiful and your scares be just scary enough."  
> C: "Happy Halloween, {name}! Whatever you're doing tonight, hope it's wickedly good. 🕷️"

### 14. Election Day (variable — 1st Tue after 1st Mon in November, even years)
> A: "Happy Election Day, {name}! Your vote is your voice — hope you make it heard today. 🗳️"  
> B: "It's Election Day, {name}! Wherever you stand, today is our collective chance to shape the future. Go vote!"  
> C: "Thinking of you on Election Day, {name}. Civic participation is one of the most powerful things we can do together."

### 15. Diwali (variable)
> A: "Happy Diwali, {name}! May this Festival of Lights fill your home with warmth, joy, and prosperity. 🪔"  
> B: "Wishing you a radiant Diwali, {name}! May the light of this beautiful celebration shine in your life all year long."  
> C: "Happy Diwali, {name}! Here's to new beginnings, inner light, and a season full of sweet celebrations."

### 16. Thanksgiving (variable — 4th Thursday of November)
> A: "Happy Thanksgiving, {name}! Grateful for you today and every day — hope your table is full and your heart is fuller."  
> B: "Wishing you the warmest Thanksgiving, {name}! May your day be cozy, delicious, and surrounded by people you love."  
> C: "Happy Thanksgiving, {name}! Among all the things I'm grateful for this year, knowing you is near the top. 🍂"

### 17. Hanukkah (variable)
> A: "Happy Hanukkah, {name}! Wishing you eight nights full of warmth, light, and joy. 🕎"  
> B: "Chag Urim Sameach, {name}! May each candle this Hanukkah light up something beautiful in your life."  
> C: "Wishing you a wonderful Hanukkah, {name}! Hope the Festival of Lights brings your family endless reasons to celebrate."

### 18. Christmas (12-25)
> A: "Merry Christmas, {name}! Wishing you a day filled with laughter, love, and every good thing the season brings. 🎄"  
> B: "Happy Christmas, {name}! Hope this holiday finds you warm, well, and surrounded by the people you cherish most."  
> C: "Season's greetings, {name}! Wishing you a Christmas that feels every bit as magical as it did when you were a kid."

---

## Recommendations

Top 5 new holidays most worth adding, ranked by impact and user relevance:

### 1. ⭐ Earth Day (April 22) — `monthDay = "04-22"`
**Why:** Fixed date, universally recognized, and culturally resonant across demographics. Arrives **tomorrow** (2026-04-22), making it the most immediate gap. Easy to implement.

### 2. ⭐ Memorial Day (Last Monday of May) — `isVariable = true`
**Why:** Major US federal holiday with near-universal recognition. Already close at hand (May 25, 2026). Great for a broad contact list.

### 3. ⭐ Martin Luther King Jr. Day (3rd Monday of January) — `isVariable = true`
**Why:** Federal holiday; high cultural significance. Complements Juneteenth (already planned). Adds important representation to the January–February range.

### 4. ⭐ St. Patrick's Day (March 17) — `monthDay = "03-17"`
**Why:** One of the most widely celebrated cultural holidays in the US regardless of Irish heritage. Fixed date, simple to add, high engagement potential.

### 5. ⭐ Labor Day (1st Monday of September) — `isVariable = true`
**Why:** Federal holiday and widely observed. Fills a gap in the late summer/fall calendar (September currently has no coverage). Variable but easy to compute as first Monday of September.

---

*Generated by Holiday Messenger weekly maintenance agent · 2026-04-21*
