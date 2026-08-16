---
name: run-frontend
description: Build, run, and drive the banking-core React frontend against the real Spring Boot backend. Use when asked to start the frontend, launch the app, take a screenshot of its UI, or interact with it (login, register, create an account, deposit/withdraw/transfer, view transaction history).
---

The frontend is a Vite + React SPA (`frontend/`) that talks to the real
banking-core backend over HTTP. There's no mock layer, so **the backend
must be up first**. Drive the running app with the REPL script at
`.claude/skills/run-frontend/driver.mjs` — it wraps Playwright behind a
line-oriented command language so an agent (or a heredoc) can pipe in a
whole scripted session without hand-rolling browser automation each
time.

All paths below are relative to `frontend/`, except the backend command
which is run from the repo root (one level up).

## Prerequisites

Node.js + npm (any recent LTS) and Playwright's Chromium build:

```bash
cd frontend
npm install
npx playwright install chromium
```

`npm install` already adds `playwright` as a devDependency; the second
command downloads the actual browser binary (cached under
`~/AppData/Local/ms-playwright/` on Windows, `~/.cache/ms-playwright/`
on Linux/Mac) — a one-time ~150MB download, skip it if that directory
already has a `chromium-*` folder.

## Build

No separate build step for local driving — `npm run dev` serves the
Vite dev server directly from source with HMR. (`npm run build` exists
for a production bundle, but the driver targets the dev server.)

## Run (agent path)

1. **Start the backend** from the repo root:

   ```bash
   docker compose up -d --build
   ```

   Wait for it to actually answer before driving anything — the driver's
   own `check-backend` command does this for you (see below), or poll
   manually:

   ```bash
   curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/auth/login
   ```

2. **Start the frontend dev server** from `frontend/`:

   ```bash
   npm run dev
   ```

   Vite serves on `http://localhost:5173`. It prints "ready" and keeps
   running in the foreground — background it or run it in a separate
   terminal/tmux pane.

3. **Drive it** with the REPL driver, piping a heredoc of commands to
   stdin (works both for a scripted one-shot run and, interactively,
   for a human typing at the `driver>` prompt):

   ```bash
   cd frontend
   SCREENSHOT_DIR=/tmp/shots node .claude/skills/run-frontend/driver.mjs <<'EOF'
   check-backend
   launch
   register-random-user
   select-option select SAVINGS
   click-text Open account
   wait .account-card
   click .account-card
   wait text=Deposit
   deposit 500
   wait .account-balance:has-text("500")
   balance
   history
   ss 05-final-after-deposit
   quit
   EOF
   ```

   This exact session was run in this container against the real
   backend and produced a `.account-balance` reading `$500` after
   deposit, a transaction-history table with a `DEPOSIT $500` row, and
   a final screenshot with no console/page errors logged.

   Env vars (all optional): `FRONTEND_URL` (default
   `http://localhost:5173`), `BACKEND_URL` (default
   `http://localhost:8080`), `SCREENSHOT_DIR` (default `/tmp/shots`).

   Screenshots land at `$SCREENSHOT_DIR/<name>.png` (full-page PNGs).

### Driver commands

| command | what it does |
|---|---|
| `check-backend` | POSTs an empty login body to the backend; a `400` means it's up. Fails fast with a clear message (and the docker command to fix it) instead of a confusing Playwright timeout later. |
| `launch` | Launches headless Chromium (`--no-sandbox`), opens a page, navigates to `FRONTEND_URL`, and wires up console/page-error logging. Must run before any other browser command. |
| `ss <name>` | Full-page screenshot to `$SCREENSHOT_DIR/<name>.png`. |
| `nav <path-or-url>` | Navigate. A bare path (`/login`) is resolved against `FRONTEND_URL`. |
| `click <selector>` | Playwright `.click(selector)`. |
| `click-text <words...>` | Clicks the first `button`/`a`/`[role=button]` whose text matches exactly, falling back to a substring match. Use when a CSS selector for the element would be fragile (no id/class, just visible text like "Open account"). |
| `fill <selector> <words...>` | Fills a single-match input. Everything after the selector is joined with spaces as the value. |
| `select-option <selector> <value>` | `.selectOption(selector, value)` for a `<select>`. |
| `wait <selector>` | Waits up to 10s for a selector to appear (Playwright's `:has-text("...")` pseudo-class works here too, e.g. `wait .account-balance:has-text("500")` — use this instead of just waiting for the element, which may already exist with stale content before an async update lands). |
| `wait-url <pattern>` | Waits for the URL to match `**<pattern>` (glob). See Gotchas — trailing-slash patterns don't match a following path segment. |
| `text <selector>` | Prints `innerText` of a selector (or `document.body` if omitted). |
| `register-random-user` | Registers a fresh user with a timestamp-unique email and a fixed password, and waits for the post-register redirect to `/`. The fastest way into an authenticated state — this app auto-logs-in on register. |
| `url` | Prints the current page URL. |
| `deposit <amount>` | On an account-detail page, fills and submits the **Deposit** form specifically (there are three inline forms — Deposit/Withdraw/Transfer — each with their own `input[type="number"]`, so a plain selector is ambiguous; this scopes to the form containing the "Deposit" label). |
| `balance` | Prints the `.account-balance` text of the current account-detail page. |
| `history` | Prints the `innerText` of the transaction-history table. |
| `help` | Lists all commands. |
| `quit` | Ends the input loop, closes the browser, exits cleanly. |

## Run (human path)

```bash
cd frontend
npm run dev
```

Open `http://localhost:5173` in a real browser. Same backend
requirement as above. `Ctrl-C` to stop.

## Test

No frontend test suite exists yet — only `npm run lint` (oxlint).
Driving the app via the REPL above is the actual verification path.

```bash
npm run lint
```

---

## Gotchas

- **`readline`'s `"close"` event races ahead of buffered `"line"`
  events on Windows with piped/heredoc stdin.** An earlier version of
  this driver used the common `rl.on("line", ...)` / `rl.on("close",
  ...)` pattern with each line chained onto a manually-tracked promise
  queue, awaited from the close handler. On Windows, `"close"` still
  fired after only the *first* queued line had run, so `quit`'s cleanup
  tore down the browser while `launch` and everything after it were
  still sitting unread in the input stream — and because two exit paths
  (the `quit` branch and the `close` handler) both raced to call
  `browser.close()`/`process.exit()`, it crashed with a native libuv
  assertion (`UV_HANDLE_CLOSING`, exit code 127) instead of just
  misbehaving quietly. The fix was to stop using the event-pair
  entirely and consume `readline` with `for await (const line of rl)`
  — Node's own backpressured async iterator, which only pulls the next
  line after the previous one's handler has resolved, and only ends
  once the stream is genuinely exhausted.
- **`/dev/stdin` doesn't exist on native Windows.** Some driver
  skeletons (electron-style) open it explicitly to read piped input;
  on Windows that's `ENOENT`. Plain `process.stdin` works fine here and
  is what this driver uses.
- **A selector that matches more than one element throws in Playwright
  "strict mode."** The account-detail page has three `input[type=
  "number"]` fields (Deposit/Withdraw/Transfer amounts). `fill
  'input[type="number"]' 500` fails ambiguously; use the dedicated
  `deposit <amount>` command instead, which scopes to the form
  containing the "Deposit" label.
- **`wait <selector>` resolving doesn't mean the value you care about
  has updated.** After submitting the deposit form, `.account-balance`
  already exists in the DOM (it existed before the deposit too), so
  `wait .account-balance` resolves instantly with the *stale* balance
  still showing — the driver reported `$0` on a real run doing this.
  Wait for the specific text instead: `wait .account-balance:has-
  text("500")`.
- **`wait-url` with a trailing-slash pattern doesn't match a path with
  more after it.** `wait-url /accounts/` times out even once the URL
  is `.../accounts/2`, because the glob `**` + `/accounts/` requires
  the URL to *end* at that slash. Wait for content on the destination
  page instead (`wait text=Deposit` worked reliably here) rather than
  the URL shape.
- **The backend has a broken `/actuator/health`** — it 500s with the
  app's generic `{"code":"INTERNAL_ERROR",...}` JSON body (no stack
  trace logged; `GlobalExceptionHandler`'s catch-all doesn't log). This
  is unrelated to the frontend and doesn't block anything — the real
  API endpoints (`/api/auth/register`, `/api/auth/login`, etc.) work
  correctly. `check-backend` deliberately probes `/api/auth/login`
  instead of `/actuator/health` for this reason.

## Troubleshooting

- **`check-backend` reports "backend not reachable"**: docker compose
  isn't up (or still starting). Run `docker compose up -d --build` from
  the repo root and wait a few seconds — the Spring Boot app takes
  ~8s to start after the container is "Up".
- **`launch` or any command prints `ERROR: launch first`**: browser
  commands were sent before `launch` (or after `quit`). Every session
  needs `launch` as its first real command, right after
  `check-backend`.
- **`click-text` reports `NOT_FOUND`**: the text match is exact-trim
  first, substring second, against `button`/`a`/`[role="button"]`
  elements only — if the label is inside a `<div>` or `<span>` wrapper
  with no clickable role, target it with `click <selector>` instead.
