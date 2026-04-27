# Holiday Messenger — Weekly Update (2026-04-27)

**Agent run date:** 2026-04-27  
**Next 90-day window:** 2026-04-27 → 2026-07-26  
**Codebase audit note:** `HolidayRepository.seedDefaultHolidays()` seeds **9 holidays**. The other 9 from the project brief (Lunar New Year, Passover, Juneteenth, Eid al-Fitr, Eid al-Adha, Rosh Hashanah, Election Day, Diwali, Hanukkah) are still absent from the code. See Recommendations.

**Since last week (2026-04-21):** Earth Day (Apr 22) has passed. Mother's Day is now 13 days out. Several new observances are covered below that were not in the 04-21 report.

---

## New Holiday Discoveries

### Imminent — Next 30 Days

#### 1. Star Wars Day — May 4 (fixed)
`monthDay = "05-04"` · **7 days away**  
Widely celebrated pop-culture day with massive social media reach. Particularly strong with younger audiences and families.

> Template A: "May the 4th be with you, {name}! Hope your day is out-of-this-galaxy good. ⭐"  
> Template B: "Happy Star Wars Day, {name}! Whether you're a Jedi or a Sith, today's a great day to celebrate with the ones you love."  
> Template C: "May the Force be with you today and always, {name}! Happy May the 4th! 🚀"

---

#### 2. Cinco de Mayo — May 5 (fixed)
`monthDay = "05-05"` · **8 days away** *(covered 04-21, fresh templates below)*

> Template A: "¡Hola, {name}! Wishing you a festive Cinco de Mayo — may your day be as bright and flavorful as the occasion deserves."  
> Template B: "Happy Cinco de Mayo, {name}! Here's hoping you find something worth celebrating today, big or small."  
> Template C: "Feliz Cinco de Mayo, {name}! A day that proves any reason to gather with good people is worth honoring. 🌮"

---

#### 3. National Nurses Day — May 6 (fixed)
`monthDay = "05-06"` · **9 days away**  
Kicks off National Nurses Week (May 6–12). With many people having nurses in their families or social circles, this is a high-impact personal message opportunity.

> Template A: "Happy National Nurses Day, {name}! The care and compassion you bring to your work changes lives every single day. Thank you."  
> Template B: "Today is for you, {name} — happy Nurses Day! What you do is genuinely heroic, and we see it."  
> Template C: "Happy National Nurses Day, {name}! Your dedication deserves more than one day of recognition, but today's a great start. 💙"

---

#### 4. Armed Forces Day — 3rd Saturday of May (variable)
`isVariable = true` · 2026 date: **May 16**  
Distinct from Veterans Day (November) — specifically honors currently serving military personnel. Complements Memorial Day well.

> Template A: "Happy Armed Forces Day, {name}! Thank you for your courage and commitment to keeping us all safe. 🇺🇸"  
> Template B: "Thinking of you today on Armed Forces Day, {name}. Your service and sacrifice mean more than words can say."  
> Template C: "Happy Armed Forces Day, {name}! Honored to know someone who gives so much in service to others."

---

#### 5. Memorial Day — Last Monday of May (variable)
`isVariable = true` · 2026 date: **May 25** *(covered 04-21, fresh templates below)*

> Template A: "Happy Memorial Day, {name}. Taking a moment today to honor those who gave everything so we could have days like this one."  
> Template B: "Wishing you a reflective and peaceful Memorial Day, {name} — grateful for the freedoms so many sacrificed to protect."  
> Template C: "Happy Memorial Day, {name}! May today be both a moment of gratitude and a chance to enjoy the life those heroes made possible. 🕊️"

---

### Next 60–90 Days

#### 6. World Environment Day — June 5 (fixed)
`monthDay = "06-05"`  
UN-designated global day focused on environmental action. Complements Earth Day and appeals to a growing audience focused on sustainability.

> Template A: "Happy World Environment Day, {name}! Every small choice we make for the planet adds up to something big. Thank you for caring. 🌿"  
> Template B: "Wishing you a meaningful World Environment Day, {name} — here's to protecting the only home we've got."  
> Template C: "Hey {name}, happy World Environment Day! Small steps, big impact — glad we're on this planet together."

---

#### 7. Pride Month — June 1 (fixed start)
`monthDay = "06-01"`  
Marking the start of LGBTQ+ Pride Month. Widely recognized and personally meaningful to a large segment of US contacts.

> Template A: "Happy Pride Month, {name}! Here's to celebrating love, identity, and the freedom to be exactly who you are. 🌈"  
> Template B: "Wishing you a joyful and vibrant Pride Month, {name} — may this June be full of color, community, and love."  
> Template C: "Happy Pride, {name}! The world is better and brighter because you're in it. Celebrate you this month. 🏳️‍🌈"

---

#### 8. Flag Day — June 14 (fixed)
`monthDay = "06-14"` *(covered 04-21, fresh templates below)*

> Template A: "Happy Flag Day, {name}! A small but meaningful reminder of the values we aspire to live up to together. 🇺🇸"  
> Template B: "Flag Day greetings, {name}! Hope today brings a moment of pride and gratitude for this country we share."  
> Template C: "Happy Flag Day, {name} — wishing you a day as bright and bold as the stars and stripes themselves."

---

#### 9. Juneteenth — June 19 (fixed)
`monthDay = "06-19"` · **Not yet in codebase** *(covered 04-21, fresh templates below)*

> Template A: "Happy Juneteenth, {name}! A day to honor the long arc of justice and celebrate freedom hard-won. ✊"  
> Template B: "Wishing you a joyful and reflective Juneteenth, {name} — may this day's legacy inspire us all to keep working toward equality."  
> Template C: "Happy Juneteenth, {name}! Today we celebrate freedom, resilience, and the people who made it possible. What an important day."

---

### Additional Notable Holidays (Outside 90-Day Window)

| Holiday | Date Rule | monthDay / isVariable | Notes |
|---|---|---|---|
| MLK Day | 3rd Monday of January | `isVariable = true` | Federal; high significance |
| St. Patrick's Day | March 17 | `monthDay = "03-17"` | Widely celebrated in US |
| International Women's Day | March 8 | `monthDay = "03-08"` | Growing recognition |
| Labor Day | 1st Monday of September | `isVariable = true` | Federal; no September coverage |
| Indigenous Peoples' Day | 2nd Monday of October | `isVariable = true` | Increasingly observed |
| Veterans Day | November 11 | `monthDay = "11-11"` | Federal; distinct from Memorial Day |
| Kwanzaa | December 26 | `monthDay = "12-26"` | Important cultural celebration |
| Presidents' Day | 3rd Monday of February | `isVariable = true` | Federal holiday |

---

## Fresh Message Templates

Three new options per existing holiday — distinct from codebase defaults **and** the 2026-04-21 suggestions.

### 1. New Year's Day (01-01)
> A: "A brand new year is here, {name}! Wishing you 365 days of growth, connection, and good surprises ahead."  
> B: "Happy New Year, {name}! Whatever this year holds, I'm glad we're in each other's lives to face it together."  
> C: "Here we go, {name} — new year, fresh start! Sending you all the best energy for what's ahead. 🎊"

### 2. Lunar New Year (variable)
> A: "Xīn Nián Kuài Lè, {name}! Wishing you a year filled with good fortune, laughter, and the warmth of family. 🏮"  
> B: "Happy Lunar New Year, {name}! May the year ahead bring you everything you hoped for and a few wonderful surprises too."  
> C: "Sending New Year blessings your way, {name}! May this season of renewal bring fresh joy into your life. 🧧"

### 3. Valentine's Day (02-14)
> A: "Happy Valentine's Day, {name}! Just wanted you to know that having you in my life is genuinely one of the good things."  
> B: "Hey {name}, happy Valentine's Day! Love comes in a million forms — grateful for the version we share. ❤️"  
> C: "Wishing you a wonderful Valentine's Day, {name}! You deserve all the love that comes your way today."

### 4. Easter (variable)
> A: "Happy Easter, {name}! Wishing you a beautiful, light-filled day with the people who matter most to you. 🌷"  
> B: "Easter greetings, {name}! May this day bring you a little more hope, a little more peace, and a lot of good food."  
> C: "Happy Easter, {name}! Here's to spring, new beginnings, and whatever you've been waiting to bloom in your life. 🐰"

### 5. Passover (variable)
> A: "Chag Sameach, {name}! Wishing you a meaningful Passover — may the stories told at your table carry forward for generations."  
> B: "Happy Passover, {name}! May this season of liberation remind you of your own strength and the power of community."  
> C: "Wishing you a joyful Passover, {name} — may your seder be full of laughter, memory, and the best matzo ball soup."

### 6. Mother's Day (variable — 2nd Sunday of May) — **MAY 10, 13 DAYS AWAY**
> A: "Happy Mother's Day, {name}! The way you show up for the people you love is something truly special. 💐"  
> B: "Thinking of you today, {name} — no card or message comes close to expressing how much you matter. Happy Mother's Day."  
> C: "Happy Mother's Day, {name}! Here's to celebrating someone who makes the world immeasurably warmer just by being in it."

### 7. Father's Day (variable — 3rd Sunday of June)
> A: "Happy Father's Day, {name}! The kind of presence you bring can't be measured — but it's felt by everyone around you."  
> B: "Wishing you a fantastic Father's Day, {name}! Hope today is filled with the rest and appreciation you absolutely deserve."  
> C: "Happy Father's Day, {name}! The quiet ways you show up every day mean more than you probably know. 🙌"

### 8. Juneteenth (06-19)
> A: "Happy Juneteenth, {name}! Today we lift our voices in celebration of freedom and the long, ongoing work of justice."  
> B: "Sending you warm Juneteenth greetings, {name} — a day to honor extraordinary people and an extraordinary legacy."  
> C: "Happy Juneteenth, {name}! May the spirit of liberation and community that defines this day carry you all year long. 🎉"

### 9. Eid al-Fitr (variable)
> A: "Eid Mubarak, {name}! Wishing you a joyful celebration after a month of reflection — may this day bring you everything you prayed for. 🌙"  
> B: "Happy Eid al-Fitr, {name}! May the sweetness of this day linger long after the celebrations end. Eid Mubarak!"  
> C: "Sending warm Eid greetings your way, {name} — may your Eid be filled with love, laughter, and the company of those who matter most."

### 10. Independence Day (07-04)
> A: "Happy Fourth of July, {name}! Wishing you a day full of summer sunshine, good food, and even better company. 🎆"  
> B: "Happy Independence Day, {name}! Freedom is worth celebrating — hope yours is loud, bright, and absolutely wonderful."  
> C: "Hey {name}, wishing you the best Fourth yet! Hope you're spending it with people who make you feel at home. 🇺🇸"

### 11. Eid al-Adha (variable)
> A: "Eid al-Adha Mubarak, {name}! May this day of reflection and giving bring deep peace and joy to you and your loved ones."  
> B: "Happy Eid al-Adha, {name}! Wishing you a blessed celebration filled with gratitude and the warmth of family. 🌙"  
> C: "Eid Mubarak, {name}! May the spirit of sacrifice and generosity that marks this day inspire you throughout the year."

### 12. Rosh Hashanah (variable)
> A: "Shanah Tovah U'Metukah, {name}! Wishing you a year as sweet as honey and as full as the table on a good holiday. 🍯"  
> B: "Happy Rosh Hashanah, {name}! May the new year bring you clarity, health, and moments of deep joy. L'Shanah Tovah!"  
> C: "Wishing you a meaningful Jewish New Year, {name} — may 5787 bring everything you're hoping for and more. 🍎"

### 13. Halloween (10-31)
> A: "Happy Halloween, {name}! Whether you're trick-or-treating or handing out candy, hope your night is delightfully spooky. 🎃"  
> B: "Sending some Halloween fun your way, {name}! May your evening be full of good scares and even better treats."  
> C: "Happy Halloween, {name}! The scariest thing about tonight is how fast this year has gone. Enjoy every spooky moment. 👻"

### 14. Election Day (variable — 1st Tuesday after 1st Monday in November, even years)
> A: "It's Election Day, {name}! Your voice matters — hope you take the chance to make it heard today. 🗳️"  
> B: "Happy Election Day, {name}! Democracy works best when everyone shows up. Go make your mark."  
> C: "Thinking of you on Election Day, {name}. Whatever the outcome, participating is always the right call. 🇺🇸"

### 15. Diwali (variable)
> A: "Happy Diwali, {name}! May your home be bright with light, your heart with joy, and your year with prosperity. 🪔"  
> B: "Wishing you a beautiful Diwali, {name}! May every lamp lit tonight bring warmth and wonder into your life."  
> C: "Happy Festival of Lights, {name}! May Diwali mark the beginning of a season full of love and abundance. ✨"

### 16. Thanksgiving (variable — 4th Thursday of November)
> A: "Happy Thanksgiving, {name}! There's so much to be grateful for — and having you in my life is genuinely near the top of the list."  
> B: "Wishing you a warm and wonderful Thanksgiving, {name}! Hope your table is full and your heart is even fuller today."  
> C: "Happy Thanksgiving, {name}! Here's to good food, good people, and a moment to pause and appreciate all we have. 🍂"

### 17. Hanukkah (variable)
> A: "Happy Hanukkah, {name}! May each night's light bring a little more warmth and wonder into your life. 🕎"  
> B: "Chag Urim Sameach, {name}! Wishing you eight nights of joy, togetherness, and the very best latkes."  
> C: "Happy Hanukkah, {name}! May the glow of the menorah remind you how much light you bring to the people around you."

### 18. Christmas (12-25)
> A: "Merry Christmas, {name}! Wishing you a holiday full of warmth, wonder, and all the people who make it special. 🎄"  
> B: "Happy Christmas, {name}! Hope this day feels slow and bright and exactly the way you hoped it would."  
> C: "Sending you peace and joy this Christmas, {name} — wishing you a holiday season that fills you right up. ⭐"

---

## Recommendations

Top 5 new holidays most worth adding right now, ranked by urgency and impact:

### 1. ⭐ Mother's Day (2nd Sunday of May) — `isVariable = true`
**Why it's #1 this week:** Already in the app's `HolidayCalendar.kt` but **absent from `seedDefaultHolidays()`** — meaning it won't fire in production. Mother's Day 2026 is **May 10, only 13 days away.** This is a critical gap to close immediately. It just needs a `Holiday(name = "Mother's Day", isVariable = true)` entry added to the seed list with a template.

### 2. ⭐ Star Wars Day (May 4) — `monthDay = "05-04"`
**Why:** Fixed date, 7 days away, extremely high engagement on social media. Light-hearted messages are easy to write and universally well-received. A quick win for the upcoming week.

### 3. ⭐ Memorial Day (Last Monday of May) — `isVariable = true`
**Why:** Major federal holiday, May 25 (28 days away). Near-universal recognition across US contacts. Fills an important gap in spring coverage.

### 4. ⭐ Juneteenth (June 19) — `monthDay = "06-19"`
**Why:** Federal holiday since 2021. Listed in the project brief as a planned addition but still absent from the codebase. Fixed date, easy to implement, high cultural significance.

### 5. ⭐ National Nurses Day (May 6) — `monthDay = "05-06"`
**Why:** 9 days away. Meaningful for a large subset of contacts (nurses, healthcare workers, and the people who love them). Short, heartfelt messages land especially well on professional recognition days.

---

*Generated by Holiday Messenger weekly maintenance agent · 2026-04-27*
