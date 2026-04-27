# Holiday Messenger — Weekly Update (2026-04-27)

**Agent run date:** 2026-04-27  
**Next 90-day window:** 2026-04-27 → 2026-07-26  
**Prior update:** 2026-04-21-weekly-update.md

### Codebase Status

`HolidayRepository.seedDefaultHolidays()` still seeds **9 holidays**. The prior update (2026-04-21) flagged 9 planned-but-missing holidays and 12 new discoveries. None have been added to the code yet. Status of most urgent prior recommendations:

| Holiday | Status | Action |
|---|---|---|
| Earth Day (04-22) | **PASSED** — not added in time | Add for 2027 |
| Cinco de Mayo (05-05) | **8 days away** | Add immediately |
| Mother's Day (05-10) | **13 days away** — already in code | No action needed |
| Memorial Day (05-25) | **28 days away** — not in code | Add before May |
| Juneteenth (06-19) | **53 days away** — not in code | Add this sprint |
| Independence Day (07-04) | **68 days away** — already in code | No action needed |

---

## New Holiday Discoveries

Holidays not covered in the prior update or in the current codebase.

### 1. National Nurses Day — May 6 (fixed)
`monthDay = "05-06"` · **9 days away**  
Observed as the start of National Nurses Week (May 6–12). High relevance for anyone with nurses in their contacts.

> Template A: "Happy National Nurses Day, {name}! The care you give every single day changes lives — thank you, truly."  
> Template B: "Thinking of you today, {name} — happy Nurses Day! The world is better because of the work you do."  
> Template C: "Happy Nurses Day, {name}! You show up for others in their hardest moments. Today we show up for you. 💙"

---

### 2. Teacher Appreciation Day — 1st Tuesday of the 1st Full Week of May (variable)
`isVariable = true` · 2026 date: **May 5** (same as Cinco de Mayo this year — coincidence)  
Part of Teacher Appreciation Week (May 4–8, 2026). Highly relevant for parents and former students.

> Template A: "Happy Teacher Appreciation Day, {name}! The difference you make in students' lives echoes for a lifetime. Thank you."  
> Template B: "Thinking of you on Teacher Appreciation Day, {name} — you are exactly the kind of teacher people never forget."  
> Template C: "Happy Teacher Appreciation Day, {name}! What you do every day is no small thing. We see you and we're grateful. 🍎"

---

### 3. Armed Forces Day — 3rd Saturday of May (variable)
`isVariable = true` · 2026 date: **May 16**  
Distinct from Veterans Day (Nov 11) and Memorial Day — specifically honors currently serving military personnel.

> Template A: "Happy Armed Forces Day, {name}. Thank you for your service and for the sacrifices you make every day."  
> Template B: "Thinking of you on Armed Forces Day, {name}. Your dedication and courage mean the world to all of us."  
> Template C: "Happy Armed Forces Day, {name}! Today we honor the men and women who stand watch so the rest of us can live freely. 🇺🇸"

---

### 4. World Environment Day — June 5 (fixed)
`monthDay = "06-05"`  
UN-recognized global day for environmental action. Complements Earth Day (April 22) as a second environmental touchpoint.

> Template A: "Happy World Environment Day, {name}! Every action we take for the planet counts — glad to share this world with someone like you."  
> Template B: "It's World Environment Day, {name}! Here's to protecting the only home we've got, one small step at a time. 🌿"  
> Template C: "Thinking of you on World Environment Day, {name} — hope today inspires us all to do a little more for the Earth we love."

---

### 5. Flag Day — June 14 (fixed) *(repeat from prior update for emphasis)*
`monthDay = "06-14"` · **48 days away**  
Not in the codebase. Covered briefly in the prior update; elevated here because it falls between Father's Day and Juneteenth in the same crowded June window.

> Template A: "Happy Flag Day, {name}! Proud to call this country home and even prouder to share it with people like you. 🇺🇸"  
> Template B: "Flag Day greetings, {name} — a quiet reminder of what we're all working to live up to together."  
> Template C: "Happy Flag Day, {name}! Sometimes it's worth pausing to remember the ideals this flag is supposed to represent."

---

### 6. World Mental Health Day — October 10 (fixed)
`monthDay = "10-10"`  
Outside the 90-day window but gaining mainstream traction every year. Appropriate for close friends and family; pairs well with a check-in message.

> Template A: "Hey {name}, today is World Mental Health Day — just wanted to reach out and say I'm thinking of you. Hope you're doing well. 💚"  
> Template B: "Happy World Mental Health Day, {name}. Checking in because you matter. How are you really doing?"  
> Template C: "World Mental Health Day, {name} — a good reminder to be kind to yourself today and to know I'm here if you need anything."

---

### 7. Grandparents Day — 1st Sunday after Labor Day (variable)
`isVariable = true` · 2026 date: **September 13**  
National holiday since 1978, underrepresented in greeting apps. Pairs naturally with Mother's Day and Father's Day as a family-oriented holiday.

> Template A: "Happy Grandparents Day, {name}! The stories, wisdom, and love you've shared are treasures I'll always carry with me."  
> Template B: "Thinking of you on Grandparents Day, {name} — you've made such a difference in my life and I'm grateful every day."  
> Template C: "Happy Grandparents Day, {name}! Some of my best memories have you in them. Thank you for everything. 🌼"

---

## Fresh Message Templates

Three new options per existing seeded holiday — all distinct from both the codebase defaults and the 2026-04-21 update.

### 1. New Year's Day (01-01)
> A: "A whole new year, {name}! Whatever you're hoping for in 2027, I hope you get it and then some."  
> B: "Happy New Year, {name}! Grateful you're in my life and excited to see what this next chapter holds for you."  
> C: "New year, same amazing you, {name}! Wishing you everything your heart is set on this year. 🎉"

### 2. Valentine's Day (02-14)
> A: "Hey {name}, just a note to say you're genuinely one of my favorite people. Happy Valentine's Day!"  
> B: "Happy Valentine's Day, {name}! Love is a lot of things — today it's this little message, just for you. 💌"  
> C: "Wishing you a Valentine's Day that feels as warm and lovely as you make the people around you feel, {name}."

### 3. Easter (variable)
> A: "Happy Easter, {name}! Hope your day is bright, your basket is full, and your heart is light. 🐰"  
> B: "Wishing you a peaceful and joy-filled Easter, {name} — spring energy is here and it's bringing good things."  
> C: "Easter greetings, {name}! Whatever this season means to you, I hope it brings you exactly what you need right now."

### 4. Mother's Day (variable — 2nd Sunday of May)
> A: "Happy Mother's Day, {name}! The way you love people is one of the most remarkable things I've ever witnessed. 🌸"  
> B: "Wishing you the most wonderful Mother's Day, {name} — you deserve a day that gives back even a fraction of what you give."  
> C: "Happy Mother's Day, {name}! Here's hoping today is full of the love and rest you're owed every single day."

### 5. Father's Day (variable — 3rd Sunday of June)
> A: "Happy Father's Day, {name}! The love and stability you bring means more than you probably know. Enjoy today."  
> B: "Hey {name}, happy Father's Day! Hope you get to kick back and not lift a finger — you've more than earned it."  
> C: "Thinking of you on Father's Day, {name}. The way you show up for the people you love is genuinely inspiring. 🎖️"

### 6. Independence Day (07-04)
> A: "Happy 4th of July, {name}! Wishing you a summer evening full of great people, cold drinks, and perfect fireworks."  
> B: "Independence Day, {name}! Hope your celebration is exactly as loud and joyful as you want it to be. 🎆"  
> C: "Happy 4th, {name}! Here's to freedom, friendship, and the kind of summer night you'll talk about for years."

### 7. Halloween (10-31)
> A: "Happy Halloween, {name}! Hope your night has the exact right amount of spooky — not too little, not too much. 🕯️"  
> B: "Hey {name}, happy Halloween! Whether you're trick-or-treating or turning off the lights and hiding — I respect it."  
> C: "Happy Halloween, {name}! Hope the vibes are eerie, the snacks are good, and nothing actually scares you tonight. 🎃"

### 8. Thanksgiving (variable — 4th Thursday of November)
> A: "Happy Thanksgiving, {name}! You're one of the things I'm most grateful for — hope today reflects that back to you. 🍂"  
> B: "Wishing you a Thanksgiving full of warmth, good food, and the people who fill you back up, {name}."  
> C: "Happy Thanksgiving, {name}! May your day be long, your table be full, and your nap afterward be legendary."

### 9. Christmas (12-25)
> A: "Merry Christmas, {name}! Hope this day holds something that makes your heart genuinely happy. 🎁"  
> B: "Happy Christmas, {name}! Wishing you the kind of holiday that feels exactly like you always hoped it would."  
> C: "Merry Christmas, {name}! Grateful for you this season and every season. May today be everything you deserve. 🌟"

---

## Recommendations

Top 5 new holidays most worth adding, updated to reflect the 2026-04-27 urgency window.

### 1. ⭐ Cinco de Mayo (May 5) — `monthDay = "05-05"` — **ACT NOW**
**Why:** 8 days away. Fixed date, widely celebrated in the US regardless of heritage. Will be missed again if not added before May 5.

### 2. ⭐ Memorial Day (Last Monday of May) — `isVariable = true` — **ACT SOON**
**Why:** Major federal holiday, 28 days away (May 25, 2026). The largest gap in the spring calendar. Near-universal recognition across all contact demographics.

### 3. ⭐ National Nurses Day (May 6) — `monthDay = "05-06"` — **ACT NOW**
**Why:** 9 days away. Newly identified this week. Highly personal and meaningful for anyone with healthcare workers in their contacts. Simple fixed-date addition.

### 4. ⭐ Juneteenth (June 19) — `monthDay = "06-19"` — **Add this sprint**
**Why:** Federal holiday since 2021, 53 days away. Culturally significant and increasingly recognized. Was called out in both the project brief and the prior update — still not in code.

### 5. ⭐ Grandparents Day (1st Sunday after Labor Day) — `isVariable = true`
**Why:** National holiday with no current coverage. Completes the "family holidays" trio alongside Mother's Day and Father's Day. September currently has zero holiday coverage in the app.

---

*Generated by Holiday Messenger weekly maintenance agent · 2026-04-27*
