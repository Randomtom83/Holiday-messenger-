# Holiday Messenger — Weekly Update
**Date:** 2026-04-16  
**Agent:** Weekly Holiday Discovery  

---

> **Note on current app state:** The actual seeded holidays in `HolidayRepository.kt` are **9**, not 18. The holidays listed in the system prompt as "current" (Lunar New Year, Passover, Juneteenth, Eid al-Fitr, Eid al-Adha, Rosh Hashanah, Election Day, Diwali, Hanukkah) are **not yet in the codebase**. They are candidates for addition.

---

## New Holiday Discoveries

Holidays NOT currently seeded in `HolidayRepository.seedDefaultHolidays()`, sorted by calendar date.

### 1. MLK Day — Martin Luther King Jr. Day
- **Date rule:** 3rd Monday of January (variable)
- **2026 date:** January 19, 2026
- **Format:** `isVariable = true` (compute via `nthDayOfWeekInMonth`)
- **Templates:**
  1. "Happy MLK Day, {name}! May his dream of justice and equality inspire us all today and always."
  2. "Thinking of you on MLK Day, {name} — a day to honor courage, compassion, and the ongoing work of justice."
  3. "Happy Martin Luther King Jr. Day, {name}! 'Darkness cannot drive out darkness; only light can do that.'"

---

### 2. Presidents' Day (Washington's Birthday)
- **Date rule:** 3rd Monday of February (variable)
- **2026 date:** February 16, 2026
- **Format:** `isVariable = true`
- **Templates:**
  1. "Happy Presidents' Day, {name}! Hope you're enjoying a well-deserved long weekend."
  2. "Wishing you a restful Presidents' Day, {name} — a great excuse to reflect on history and take a break."
  3. "Hey {name}, happy Presidents' Day! Hope the day treats you as well as a holiday should."

---

### 3. St. Patrick's Day
- **Date:** March 17 (fixed)
- **MonthDay:** `03-17`
- **Templates:**
  1. "Happy St. Patrick's Day, {name}! May the luck of the Irish be with you all day long."
  2. "Wearing green and thinking of you, {name} — happy St. Paddy's Day!"
  3. "Hope your St. Patrick's Day is filled with good craic, {name}! Sláinte!"

---

### 4. International Women's Day
- **Date:** March 8 (fixed)
- **MonthDay:** `03-08`
- **Templates:**
  1. "Happy International Women's Day, {name}! Celebrating the incredible woman you are today and every day."
  2. "Sending you love on International Women's Day, {name} — the world is better with you in it."
  3. "Here's to you, {name} — happy International Women's Day! Your strength and spirit are an inspiration."

---

### 5. Earth Day
- **Date:** April 22 (fixed)
- **MonthDay:** `04-22`
- **2026 date:** April 22, 2026 *(in next 90 days — 6 days away)*
- **Templates:**
  1. "Happy Earth Day, {name}! Grateful to share this beautiful planet with you."
  2. "Hey {name}, happy Earth Day! Hope it inspires a little extra love for the world around us."
  3. "Thinking of you on Earth Day, {name} — small acts of care add up to big change."

---

### 6. Administrative Professionals' Day
- **Date rule:** Last Wednesday of April (variable)
- **2026 date:** April 29, 2026 *(in next 90 days)*
- **Format:** `isVariable = true`
- **Templates:**
  1. "Happy Administrative Professionals' Day, {name}! Your hard work keeps everything running — thank you."
  2. "Hey {name}, today is your day! Administrative Professionals' Day is a reminder of how invaluable you are."
  3. "Wishing you a wonderful Administrative Professionals' Day, {name} — you make a difference every single day."

---

### 7. Cinco de Mayo
- **Date:** May 5 (fixed)
- **MonthDay:** `05-05`
- **2026 date:** May 5, 2026 *(in next 90 days)*
- **Templates:**
  1. "Happy Cinco de Mayo, {name}! Hope your day is full of great food, music, and celebration."
  2. "Feliz Cinco de Mayo, {name}! Wishing you a festive and fun day."
  3. "Hey {name}, happy Cinco de Mayo! A perfect excuse for tacos and good company."

---

### 8. Memorial Day
- **Date rule:** Last Monday of May (variable)
- **2026 date:** May 25, 2026 *(in next 90 days)*
- **Format:** `isVariable = true`
- **Templates:**
  1. "Happy Memorial Day, {name}. Grateful for those who gave everything so we could live freely."
  2. "Thinking of you on Memorial Day, {name} — a day to honor, remember, and give thanks."
  3. "Hey {name}, hope you have a meaningful Memorial Day — one of reflection and gratitude for our heroes."

---

### 9. Armed Forces Day
- **Date rule:** 3rd Saturday of May (variable)
- **2026 date:** May 16, 2026 *(in next 90 days)*
- **Format:** `isVariable = true`
- **Templates:**
  1. "Happy Armed Forces Day, {name}! Thank you for your service and sacrifice."
  2. "Honoring all who serve on Armed Forces Day — thinking of you, {name}."
  3. "Hey {name}, happy Armed Forces Day! A grateful nation salutes those in uniform."

---

### 10. Eid al-Adha
- **Date:** Variable (Islamic lunar calendar)
- **2026 date:** ~May 27, 2026 *(in next 90 days)*
- **Format:** `isVariable = true` (requires Islamic calendar calculation or manual yearly update)
- **Templates:**
  1. "Eid Mubarak, {name}! May this Eid al-Adha bring blessings, peace, and joy to you and your family."
  2. "Wishing you a blessed Eid al-Adha, {name}! May the spirit of sacrifice and devotion fill your heart."
  3. "Happy Eid al-Adha, {name}! Sending love and warmest wishes to you and yours on this blessed day."

---

### 11. Flag Day
- **Date:** June 14 (fixed)
- **MonthDay:** `06-14`
- **2026 date:** June 14, 2026 *(in next 90 days)*
- **Templates:**
  1. "Happy Flag Day, {name}! Proud to wave the red, white, and blue alongside great people like you."
  2. "Hey {name}, happy Flag Day! A day to celebrate the symbol of freedom and unity."
  3. "Wishing you a wonderful Flag Day, {name} — may the stars and stripes always stand for hope and liberty."

---

### 12. Juneteenth
- **Date:** June 19 (fixed)
- **MonthDay:** `06-19`
- **2026 date:** June 19, 2026 *(in next 90 days)*
- **Templates:**
  1. "Happy Juneteenth, {name}! Celebrating freedom, resilience, and the long arc of justice."
  2. "Wishing you a meaningful Juneteenth, {name} — a day to honor history and the ongoing journey toward equality."
  3. "Happy Juneteenth, {name}! May the spirit of liberation and community inspire us all."

---

### 13. Labor Day
- **Date rule:** 1st Monday of September (variable)
- **2026 date:** September 7, 2026
- **Format:** `isVariable = true`
- **Templates:**
  1. "Happy Labor Day, {name}! Enjoy the long weekend — you've more than earned it."
  2. "Hey {name}, happy Labor Day! A well-deserved day off for someone who works as hard as you do."
  3. "Wishing you a relaxing Labor Day, {name} — today is all about celebrating you."

---

### 14. Indigenous Peoples' Day
- **Date rule:** 2nd Monday of October (variable)
- **2026 date:** October 12, 2026
- **Format:** `isVariable = true`
- **Templates:**
  1. "Happy Indigenous Peoples' Day, {name}! A day to honor the rich cultures and enduring strength of Native communities."
  2. "Wishing you a thoughtful Indigenous Peoples' Day, {name} — a day of recognition, respect, and gratitude."
  3. "Hey {name}, happy Indigenous Peoples' Day! Today we celebrate resilience, heritage, and the wisdom of Native peoples."

---

### 15. Veterans Day
- **Date:** November 11 (fixed)
- **MonthDay:** `11-11`
- **Templates:**
  1. "Happy Veterans Day, {name}. With deep gratitude for your service and sacrifice."
  2. "Thinking of you on Veterans Day, {name} — thank you for defending the freedoms we hold dear."
  3. "Hey {name}, happy Veterans Day! Your service matters more than words can express."

---

### 16. Kwanzaa
- **Date:** December 26 (fixed, runs Dec 26–Jan 1)
- **MonthDay:** `12-26`
- **Templates:**
  1. "Happy Kwanzaa, {name}! May the seven principles guide you toward unity, creativity, and purpose this season."
  2. "Wishing you a joyful Kwanzaa, {name} — celebrating heritage, community, and the light of family."
  3. "Happy first day of Kwanzaa, {name}! May this week be filled with reflection, gratitude, and connection."

---

### 17. Lunar New Year
- **Date:** Variable (Chinese lunar calendar, late Jan–mid Feb)
- **2026 date:** February 17, 2026 (Year of the Horse)
- **Format:** `isVariable = true` (requires lunar calendar calculation or manual update)
- **Templates:**
  1. "Happy Lunar New Year, {name}! Wishing you health, happiness, and good fortune in the Year of the Horse."
  2. "Xin Nian Kuai Le, {name}! May the new year bring you joy, prosperity, and all you hope for."
  3. "Happy Lunar New Year, {name}! A time for new beginnings — may this year be your best one yet."

---

### 18. Eid al-Fitr
- **Date:** Variable (Islamic lunar calendar)
- **2026 date:** ~March 20, 2026
- **Format:** `isVariable = true`
- **Templates:**
  1. "Eid Mubarak, {name}! May this Eid al-Fitr bring you joy, peace, and the blessings of the season."
  2. "Happy Eid al-Fitr, {name}! Wishing you and your loved ones a beautiful and blessed celebration."
  3. "Sending warm Eid wishes your way, {name} — may this day be filled with happiness and gratitude."

---

### 19. Rosh Hashanah
- **Date:** Variable (Jewish lunar calendar, Sept–Oct)
- **2026 date:** ~September 11, 2026
- **Format:** `isVariable = true`
- **Templates:**
  1. "Shana Tova, {name}! Wishing you a sweet, healthy, and fulfilling New Year."
  2. "Happy Rosh Hashanah, {name}! May the year ahead bring you joy, renewal, and all good things."
  3. "L'shanah tovah, {name}! Sending warm wishes for a wonderful Jewish New Year filled with blessings."

---

### 20. Diwali
- **Date:** Variable (Hindu lunar calendar, Oct–Nov)
- **2026 date:** ~October 19, 2026
- **Format:** `isVariable = true`
- **Templates:**
  1. "Happy Diwali, {name}! May the Festival of Lights fill your home with warmth, love, and prosperity."
  2. "Wishing you a radiant Diwali, {name} — may light overcome darkness and joy fill every corner of your life."
  3. "Happy Diwali, {name}! Sending sparkle, sweetness, and all the blessings of the season your way."

---

### 21. Hanukkah
- **Date:** Variable (Jewish lunar calendar, late Nov–late Dec)
- **2026 date:** ~December 4, 2026
- **Format:** `isVariable = true`
- **Templates:**
  1. "Happy Hanukkah, {name}! May the light of the menorah shine bright in your home all eight nights."
  2. "Chag Sameach, {name}! Wishing you a joyful Festival of Lights filled with warmth and family."
  3. "Happy Hanukkah, {name}! May each candle bring a little more light, love, and latkes into your life."

---

### 22. Passover
- **Date:** Variable (Jewish lunar calendar, Mar–Apr)
- **2026 date:** ~April 1–9, 2026 (just passed for this year)
- **Format:** `isVariable = true`
- **Templates:**
  1. "Chag Pesach Sameach, {name}! May your Passover Seder be full of meaning, family, and good food."
  2. "Happy Passover, {name}! Wishing you a joyful celebration of freedom and remembrance."
  3. "Wishing you a meaningful Passover, {name} — may the spirit of liberation inspire your year ahead."

---

### 23. Election Day
- **Date rule:** 1st Tuesday after 1st Monday in November (variable)
- **2026 date:** November 3, 2026 (midterms)
- **Format:** `isVariable = true`
- **Templates:**
  1. "Happy Election Day, {name}! Your voice matters — hope you had a chance to make it heard."
  2. "Hey {name}, it's Election Day! Democracy looks good on us — hope you voted."
  3. "Happy Election Day, {name}! Wishing for a peaceful, fair, and hopeful outcome for us all."

---

## Fresh Message Templates

New options for the 9 existing holidays (different from the current defaults in the codebase).

### New Year's Day
1. "Hey {name}, the new year is here — may it be full of adventures, laughter, and things that make your heart happy."
2. "{name}, out with the old and in with the new! Here's to a year of growth, joy, and good times together."
3. "Wishing you a sparkling new year, {name}. May 365 fresh days bring you everything you're hoping for."

### Valentine's Day
1. "Hey {name}, just wanted to take Valentine's Day as an excuse to say I'm really glad you're in my life."
2. "Happy Valentine's Day, {name}! Whether you're celebrating with someone special or treating yourself — you deserve all the love."
3. "{name}, sending you a little extra warmth on this cozy February day. Happy Valentine's Day!"

### Easter
1. "Happy Easter, {name}! Hope your day is bright, your basket is full, and you're surrounded by people you love."
2. "Wishing you a joyful Easter, {name} — filled with springtime happiness and sweet moments."
3. "Happy Easter, {name}! May the season of renewal bring fresh hope and beautiful beginnings your way."

### Mother's Day
1. "{name}, today is all about celebrating extraordinary people — and you fit that description perfectly. Happy Mother's Day!"
2. "Happy Mother's Day, {name}! The love you pour into the world comes back multiplied — you deserve every bit of it."
3. "Thinking of you on Mother's Day, {name}. The warmth and care you give to others is a true gift."

### Father's Day
1. "Happy Father's Day, {name}! Here's to the dads who show up, step up, and never stop — you're one of them."
2. "{name}, wishing you a fantastic Father's Day filled with zero obligations and all the good stuff."
3. "Happy Father's Day to one of the greats, {name}! Hope today is exactly your kind of perfect."

### Independence Day
1. "Happy 4th of July, {name}! BBQ, fireworks, and good company — the American dream in one afternoon."
2. "Here's to freedom, fireworks, and friendship — happy Independence Day, {name}!"
3. "Hey {name}, hope your 4th is loud, bright, and surrounded by people who make you feel at home. Happy Independence Day!"

### Halloween
1. "Happy Halloween, {name}! Whether you're scaring or being scared — hope it's a frightfully good time."
2. "Hey {name}, it's the spookiest night of the year! Hope your Halloween is full of treats and zero tricks."
3. "Wishing you a hauntingly fun Halloween, {name} — may the candy be plentiful and the scares just scary enough."

### Thanksgiving
1. "Happy Thanksgiving, {name}! If there's one thing I'm thankful for, it's people like you in my life."
2. "{name}, wishing you a warm, full, and joyful Thanksgiving — may the table be long and the conversation even longer."
3. "Happy Thanksgiving, {name}! Today's a great reminder to pause and appreciate the good things. You're one of mine."

### Christmas
1. "Merry Christmas, {name}! Wishing you a holiday filled with warmth, wonder, and the people who matter most."
2. "Hey {name}, hope Christmas brings you everything that makes you smile — and a little extra magic too."
3. "Sending warm Christmas wishes your way, {name}. May the season wrap you in peace, joy, and good company."

---

## Recommendations

**Top 5 new holidays most worth adding to the app**, ranked by impact and relevance:

### 1. Memorial Day *(Priority: HIGH)*
- **Why:** Federal holiday with near-universal US recognition. Last Monday of May — well understood and widely observed. Already a gap in the app alongside Labor Day. Very appropriate for sending warm messages to friends and family.
- **2026 date:** May 25 — coming up in 39 days.

### 2. Labor Day *(Priority: HIGH)*
- **Why:** The other major US summer federal holiday. Completes the trio with Memorial Day and Independence Day (already in app). First Monday of September — easy to compute.

### 3. MLK Day *(Priority: HIGH)*
- **Why:** Federal holiday, deeply meaningful, and increasingly prominent in mainstream culture. Third Monday of January. Provides a meaningful early-year touchpoint.

### 4. Juneteenth *(Priority: HIGH)*
- **Why:** Federal holiday since 2021, June 19 fixed date — easy to implement. Growing cultural significance and recognition. Completes the American holiday calendar. Coming up June 19 (63 days away).

### 5. St. Patrick's Day *(Priority: MEDIUM-HIGH)*
- **Why:** March 17 fixed date — trivially simple to add. Extremely popular in mainstream US culture, widely celebrated across all backgrounds. High engagement holiday for messaging apps.

**Runners-up worth considering:**
- **Earth Day** (April 22): Easy fixed date, growing cultural relevance, good for eco-conscious contacts.
- **Veterans Day** (November 11): Fixed date, federal holiday, meaningful messaging opportunity.
- **Lunar New Year**: Broadens cultural coverage significantly; requires lunar calendar lookup for variable date.
- **Diwali**: Popular and growing US observance; broadens the app's cultural inclusivity.

---

*Generated by Weekly Holiday Discovery Agent — 2026-04-16*
