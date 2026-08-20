// REPL driver for the Banking Core frontend (React + Vite), against the real
// backend (Spring Boot + Postgres). Run under Node from anywhere; module
// resolution for "playwright" comes from frontend/node_modules, so this file
// stays inside frontend/ even though it's driven from the skill directory.
//
// Designed for agents: wrap in tmux, send-keys commands, capture-pane output.
// Or pipe a heredoc to stdin for a one-shot flow (see SKILL.md).
import { chromium } from "playwright";
import * as readline from "node:readline";
import * as fs from "node:fs";
import * as path from "node:path";

const FRONTEND_URL = process.env.FRONTEND_URL || "http://localhost:5173";
const BACKEND_URL = process.env.BACKEND_URL || "http://localhost:8080";
const SHOT_DIR = process.env.SCREENSHOT_DIR || "/tmp/shots";
fs.mkdirSync(SHOT_DIR, { recursive: true });

let browser = null;
let page = null;

const COMMANDS = {
  // Fails fast with a clear message instead of a confusing Playwright
  // timeout if the backend (docker compose) isn't up yet.
  async "check-backend"() {
    try {
      const res = await fetch(`${BACKEND_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: "{}",
      });
      // 400 (validation error on an empty body) means the API is up and answering.
      console.log(res.status === 400 ? "backend OK (400 on empty login body, as expected)" : `backend responded ${res.status}`);
    } catch (e) {
      console.log("ERROR: backend not reachable at", BACKEND_URL, "-", e.message, "- run `docker compose up -d --build` from the repo root first.");
    }
  },

  async launch() {
    if (browser) return console.log("already launched");
    browser = await chromium.launch({ args: ["--no-sandbox"] });
    page = await browser.newPage();
    page.on("console", (msg) => {
      if (msg.type() === "error") console.log("[console error]", msg.text());
    });
    page.on("pageerror", (err) => console.log("[page error]", err.message));
    await page.goto(FRONTEND_URL, { waitUntil: "networkidle" });
    console.log("launched. current url:", page.url());
  },

  async ss(name) {
    if (!page) return console.log("ERROR: launch first");
    const f = path.join(SHOT_DIR, (name || `ss-${Date.now()}`) + ".png");
    await page.screenshot({ path: f, fullPage: true });
    console.log("screenshot:", f);
  },

  async nav(target) {
    if (!page) return console.log("ERROR: launch first");
    const url = target.startsWith("http") ? target : `${FRONTEND_URL}${target}`;
    await page.goto(url, { waitUntil: "networkidle" });
    console.log("nav ->", page.url());
  },

  async click(sel) {
    if (!page) return console.log("ERROR: launch first");
    try {
      await page.click(sel);
      console.log("click", sel, "-> OK");
    } catch (e) {
      console.log("click", sel, "-> ERROR:", e.message);
    }
  },

  async "click-text"(...words) {
    if (!page) return console.log("ERROR: launch first");
    const text = words.join(" ");
    const result = await page.evaluate((t) => {
      const els = [...document.querySelectorAll("button, a, [role=\"button\"]")];
      const el = els.find((e) => e.textContent?.trim() === t) ?? els.find((e) => e.textContent?.includes(t));
      if (!el) return "NOT_FOUND";
      el.click();
      return "OK: " + el.tagName;
    }, text);
    console.log("click-text", JSON.stringify(text), "->", result);
  },

  async fill(sel, ...words) {
    if (!page) return console.log("ERROR: launch first");
    const value = words.join(" ");
    // Selector-based heuristic, not a real secret-detector - good enough to
    // stop a password typed for a `password`-ish selector from landing in
    // plain text in this log (and from there, in whatever captured this
    // command's output - a terminal scrollback, a piped log file, etc).
    const isSecret = /password|secret|token/i.test(sel);
    try {
      await page.fill(sel, value);
      console.log("fill", sel, "->", isSecret ? "*".repeat(value.length) : value);
    } catch (e) {
      console.log("fill", sel, "-> ERROR:", e.message);
    }
  },

  async "select-option"(sel, value) {
    if (!page) return console.log("ERROR: launch first");
    await page.selectOption(sel, value);
    console.log("select-option", sel, "->", value);
  },

  async wait(sel) {
    if (!page) return console.log("ERROR: launch first");
    try {
      await page.waitForSelector(sel, { timeout: 10_000 });
      console.log("found:", sel);
    } catch {
      console.log("TIMEOUT:", sel);
    }
  },

  async "wait-url"(pattern) {
    if (!page) return console.log("ERROR: launch first");
    try {
      await page.waitForURL(`**${pattern}`, { timeout: 10_000 });
      console.log("url matched:", page.url());
    } catch {
      console.log("TIMEOUT waiting for url pattern:", pattern, "- currently at", page.url());
    }
  },

  async text(sel) {
    if (!page) return console.log("ERROR: launch first");
    console.log(await page.evaluate((s) => (s ? document.querySelector(s) : document.body)?.innerText ?? "(null)", sel || null));
  },

  // Registers a fresh unique user and lands on the accounts page - the
  // fastest way into an authenticated state (register auto-logs-in here).
  async "register-random-user"() {
    if (!page) return console.log("ERROR: launch first");
    const email = `demo-${Date.now()}@example.com`;
    await page.goto(`${FRONTEND_URL}/register`, { waitUntil: "networkidle" });
    await page.fill('input[type="email"]', email);
    await page.fill('input[type="password"]', "supersecret123");
    await page.click('button[type="submit"]');
    await page.waitForURL(FRONTEND_URL + "/");
    console.log("registered and logged in as:", email);
  },

  // Logs in with existing credentials (e.g. the seeded admin) instead of
  // registering a fresh user - waits for the post-login redirect to "/",
  // via content instead of wait-url (see the wait-url gotcha in SKILL.md:
  // its "/" pattern can report a match before the redirect actually lands).
  async login(email, password) {
    if (!page) return console.log("ERROR: launch first");
    await page.goto(`${FRONTEND_URL}/login`, { waitUntil: "networkidle" });
    await page.fill('input[type="email"]', email);
    await page.fill('input[type="password"]', password);
    await page.click('button[type="submit"]');
    await page.waitForSelector("text=Accounts", { timeout: 10_000 });
    console.log("logged in as:", email);
  },

  async url() {
    if (!page) return console.log("ERROR: launch first");
    console.log(page.url());
  },

  // Account-detail page has three inline forms (Deposit/Withdraw/Transfer)
  // each with their own `input[type="number"]`, so a plain CSS selector for
  // "the amount field" is ambiguous (Playwright's strict mode rejects a
  // multi-match .fill()). Scope to the form containing the "Deposit" label.
  async deposit(amount) {
    if (!page) return console.log("ERROR: launch first");
    const form = page.locator("form", { hasText: "Deposit" });
    await form.locator('input[type="number"]').fill(String(amount));
    await form.locator('button[type="submit"]').click();
    console.log("deposit submitted:", amount);
  },

  async balance() {
    if (!page) return console.log("ERROR: launch first");
    console.log(await page.locator(".account-balance").first().textContent());
  },

  async history() {
    if (!page) return console.log("ERROR: launch first");
    console.log(await page.locator("table.table").first().innerText());
  },

  help() {
    // "quit" isn't dispatched through this map (see the "line" handler
    // below) - it's the one thing that has to close the browser AND end
    // the REPL together, so it gets special-cased instead of being just
    // another async command.
    console.log("commands:", [...Object.keys(COMMANDS), "quit"].join(", "));
  },
};

// Plain process.stdin (not the /dev/stdin fd trick some Electron drivers use -
// that's only needed when the app under test might steal stdin, which a
// regular Playwright-driven browser never does, and /dev/stdin doesn't even
// exist on native Windows).
//
// NOTE on why this is a `for await...of` loop and not the more common
// rl.on("line", ...) + rl.on("close", ...) event-pair: with piped/heredoc
// input on Windows, readline's "close" event (tied to the input stream's
// "end") can fire before every buffered "line" has actually been emitted -
// an earlier version here queued each "line" onto a manually-chained
// Promise and awaited that queue from the "close" handler, but "close" still
// won the race after only the first line, so `quit`'s cleanup ran while
// later commands (e.g. "launch") were still sitting unread in the stream.
// `for await...of rl` sidesteps the whole event-ordering question: it's
// Node's own backpressured async iterator over the interface, so the next
// line is only pulled once the previous loop body (our awaited command) has
// finished, and the loop only ends once the stream is truly exhausted.
const rl = readline.createInterface({ input: process.stdin, output: process.stdout });

console.log('banking-core frontend driver - "help" for commands, "check-backend" then "launch" to start');
process.stdout.write("driver> ");

for await (const line of rl) {
  const [cmd, ...rest] = line.trim().split(/\s+/);
  if (cmd) {
    if (cmd === "quit") {
      break;
    } else {
      const fn = COMMANDS[cmd];
      if (!fn) {
        console.log("unknown:", cmd, "- try: help");
      } else {
        try {
          await fn(...rest);
        } catch (e) {
          console.log("ERROR:", e.message);
        }
      }
    }
  }
  process.stdout.write("driver> ");
}

rl.close();
if (browser) await browser.close().catch(() => {});
console.log("closed");
process.exit(0);
