/* CourseCompass content script
 * Injects an "Analyze" button for each course row on louslist.org and calls local backend.
 */

// Production API base URL.
// After deployment, set this to: https://<your-backend-host>/api/v1/analyze
// For local development, keep localhost.
const API_BASE = "http://localhost:8080/api/v1/analyze";

function normalizeWhitespace(s) {
  return (s || "").replace(/\s+/g, " ").trim();
}

function parseCourseRow(tr) {
  // Lou's List pages can vary, so we heuristically extract from the row text.
  // Expected to find something like: "CS 3240 ... Sherriff, Mark ..."
  const text = normalizeWhitespace(tr.innerText);

  // Course ID heuristic: department + number (e.g., "CS 3240", "STS 4500")
  const courseMatch = text.match(/\b([A-Z]{2,4})\s*(\d{4})\b/);
  const courseId = courseMatch ? `${courseMatch[1]} ${courseMatch[2]}` : null;

  // Professor heuristic: "Last, First" pattern (capitalized, comma)
  // Avoid matching common table headers.
  const profMatch = text.match(/\b([A-Z][a-zA-Z'\-]+,\s*[A-Z][a-zA-Z'\-]+)\b/);
  const professorName = profMatch ? profMatch[1] : null;

  return { courseId, professorName, text };
}

function createTooltip() {
  const tip = document.createElement("div");
  tip.style.position = "absolute";
  tip.style.zIndex = "999999";
  tip.style.maxWidth = "280px";
  tip.style.padding = "8px 10px";
  tip.style.border = "1px solid rgba(0,0,0,.15)";
  tip.style.borderRadius = "8px";
  tip.style.background = "#fff";
  tip.style.boxShadow = "0 8px 24px rgba(0,0,0,.12)";
  tip.style.fontSize = "12px";
  tip.style.lineHeight = "1.35";
  tip.style.color = "#111";
  tip.style.display = "none";
  document.body.appendChild(tip);
  return tip;
}

const tooltip = createTooltip();

function showTooltipNear(el, html) {
  tooltip.innerHTML = html;
  const rect = el.getBoundingClientRect();
  const top = window.scrollY + rect.top;
  const left = window.scrollX + rect.right + 10;
  tooltip.style.top = `${top}px`;
  tooltip.style.left = `${left}px`;
  tooltip.style.display = "block";
}

function hideTooltip() {
  tooltip.style.display = "none";
}

async function analyze(professorName, courseId) {
  const url = new URL(API_BASE);
  url.searchParams.set("prof", professorName);
  url.searchParams.set("course", courseId);

  const res = await fetch(url.toString(), {
    method: "GET",
    headers: { "Accept": "application/json" }
  });

  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new Error(`Backend error ${res.status}: ${body || res.statusText}`);
  }

  return res.json();
}

function maybeInjectButtons() {
  const rows = Array.from(document.querySelectorAll("tr"));
  if (!rows.length) return;

  for (const tr of rows) {
    if (tr.dataset.courseCompassInjected === "true") continue;

    const { professorName, courseId } = parseCourseRow(tr);
    if (!professorName || !courseId) continue;

    // Try to inject into the last cell; fallback to end of row.
    const cells = tr.querySelectorAll("td, th");
    const targetCell = cells.length ? cells[cells.length - 1] : tr;

    const btn = document.createElement("button");
    btn.type = "button";
    btn.textContent = "Analyze 🔍";
    btn.style.marginLeft = "8px";
    btn.style.padding = "4px 8px";
    btn.style.borderRadius = "8px";
    btn.style.border = "1px solid rgba(0,0,0,.2)";
    btn.style.background = "#f8fafc";
    btn.style.cursor = "pointer";
    btn.style.fontSize = "12px";

    btn.addEventListener("click", async (e) => {
      e.preventDefault();
      e.stopPropagation();

      btn.disabled = true;
      const prevText = btn.textContent;
      btn.textContent = "Analyzing…";

      try {
        const data = await analyze(professorName, courseId);
        const rating = data.avgRating ?? "N/A";
        const vibe = data.sentimentSummary ?? data.vibe ?? "";

        // Tooltip + fallback alert.
        const html = `
          <div style="font-weight: 700; margin-bottom: 4px;">CourseCompass</div>
          <div><span style="color:#334155;">${courseId}</span> — <span style="color:#334155;">${professorName}</span></div>
          <div style="margin-top: 6px;"><b>Rating:</b> ${rating}</div>
          <div style="margin-top: 4px;"><b>Vibe:</b> ${vibe}</div>
          <div style="margin-top: 8px; color:#64748b;">(Click anywhere to close)</div>
        `;

        showTooltipNear(btn, html);
      } catch (err) {
        alert(`CourseCompass failed: ${err?.message || err}`);
      } finally {
        btn.disabled = false;
        btn.textContent = prevText;
      }
    });

    targetCell.appendChild(btn);
    tr.dataset.courseCompassInjected = "true";
  }
}

// Close tooltip on click outside.
document.addEventListener("click", (e) => {
  if (tooltip.style.display === "none") return;
  if (tooltip.contains(e.target)) return;
  hideTooltip();
});

// Initial run + observe changes (some pages update dynamically).
maybeInjectButtons();

const observer = new MutationObserver(() => {
  // Keep it cheap.
  maybeInjectButtons();
});

observer.observe(document.documentElement, { childList: true, subtree: true });
