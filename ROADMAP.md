# Transit Planner Roadmap: Feature-by-Feature

## Current State

**User can:**
- Create a route (name + origin + destination)
- See list of all routes

**Limitations:**
- Routes disappear on server restart
- No login - everyone sees all routes
- No edit or delete
- No real transit data

---

## Feature Roadmap

### Feature 1: User Accounts
**User story:** As a user, I can sign up and log in so my routes are private to me.

| UI Element | Description |
|------------|-------------|
| Sign up page | Create account with email (via Clerk) |
| Login page | Sign in with email/password or social |
| User menu | Avatar in header, logout option |
| Protected routes | Redirect to login if not authenticated |

**Backend:** Clerk JWT validation, User table, routes filtered by user

---

### Feature 2: Persistent Routes
**User story:** As a user, my saved routes persist even after I close the browser.

| UI Element | Description |
|------------|-------------|
| (No new UI) | Existing create/list now persists to database |
| Loading states | Spinners while fetching routes |
| Error messages | "Failed to save route" toast notifications |

**Backend:** PostgreSQL database, JPA entities, Flyway migrations

---

### Feature 3: Route Management
**User story:** As a user, I can view, edit, and delete my saved routes.

| UI Element | Description |
|------------|-------------|
| Route detail page | `/routes/:id` - view single route with all info |
| Edit button | Open edit form with pre-filled values |
| Delete button | Confirm dialog → remove route |
| Route cards | Clickable cards that navigate to detail |

**Backend:** GET/PUT/DELETE `/api/routes/{id}` endpoints

---

### Feature 4: Transport Options
**User story:** As a user, I can add specific transit lines to my route (e.g., "Take Metro 1 from Merode to Schuman").

| UI Element | Description |
|------------|-------------|
| Add transport option form | Select provider (STIB/SNCB), mode (metro/bus/train), line, stops |
| Transport option list | Show all options on route detail page |
| Remove option button | Delete individual transport options |
| Default option selector | Mark one option as preferred |

**Backend:** TransportOption entity, transport_options table, CRUD endpoints

---

### Feature 5: Stop & Line Search
**User story:** As a user, I can search for stops and lines instead of typing IDs manually.

| UI Element | Description |
|------------|-------------|
| Stop autocomplete | Type "Merode" → see matching stops with provider |
| Line selector | Dropdown of available lines at selected stop |
| Direction picker | Choose direction based on terminus |
| Map preview | (Optional) Show stop location on mini map |

**Backend:** `/api/stops/search`, `/api/lines` endpoints, transit provider data

---

### Feature 6: Live Departures
**User story:** As a user, I can see real-time departure times for my saved routes.

| UI Element | Description |
|------------|-------------|
| Departure board | "Next metro in 3 min, 8 min, 14 min" |
| Real-time indicator | Pulsing dot for live data vs scheduled |
| Refresh button | Manual refresh of departure times |
| Auto-refresh | Update every 30 seconds automatically |

**Backend:** STIB/SNCB/TfL API adapters, `/api/departures` endpoint

---

### Feature 7: Quick Actions (Home Dashboard)
**User story:** As a user, I see my routes on the home page with one-tap access to departures.

| UI Element | Description |
|------------|-------------|
| Route cards on home | Show top 5 routes with next departure |
| Quick departure view | Tap card → expand to show departure times |
| Favorite routes | Star routes to pin them to top |
| City selector | Switch between Brussels/London |

**Backend:** User preferences, favorite routes flag

---

### Feature 8: Multi-City Support
**User story:** As a user traveling between Brussels and London, I can manage routes in both cities.

| UI Element | Description |
|------------|-------------|
| City toggle | Switch context between Brussels/London |
| City indicator | Badge on routes showing which city |
| TfL integration | London tube, bus, rail departures |

**Backend:** TfL API adapter, city field on routes

---

## Implementation Order

```
Feature 1 (Accounts) ──┬──→ Feature 2 (Persistence) ──→ Feature 3 (CRUD)
                       │
                       └──→ Feature 4 (Transport Options) ──→ Feature 5 (Search)
                                                                    │
                                                                    ↓
                            Feature 7 (Dashboard) ←── Feature 6 (Departures)
                                    │
                                    ↓
                            Feature 8 (Multi-City)
```

**Suggested order:** 1 → 2 → 3 → 4 → 6 → 5 → 7 → 8

- Features 1-3: Foundation (auth + CRUD)
- Features 4-6: Core value (transit data)
- Features 7-8: Polish (UX + expansion)

---

## MVP Milestone

After **Features 1-4**, you have a functional MVP:
- Users can sign up and log in
- Create/edit/delete routes with transport options
- Data persists in database
- Routes are user-scoped

After **Feature 6**, you have the core product:
- Live departure times make the app actually useful
