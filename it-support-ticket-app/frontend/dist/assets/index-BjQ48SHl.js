(function(){const r=document.createElement("link").relList;if(r&&r.supports&&r.supports("modulepreload"))return;for(const e of document.querySelectorAll('link[rel="modulepreload"]'))o(e);new MutationObserver(e=>{for(const t of e)if(t.type==="childList")for(const i of t.addedNodes)i.tagName==="LINK"&&i.rel==="modulepreload"&&o(i)}).observe(document,{childList:!0,subtree:!0});function s(e){const t={};return e.integrity&&(t.integrity=e.integrity),e.referrerPolicy&&(t.referrerPolicy=e.referrerPolicy),e.crossOrigin==="use-credentials"?t.credentials="include":e.crossOrigin==="anonymous"?t.credentials="omit":t.credentials="same-origin",t}function o(e){if(e.ep)return;e.ep=!0;const t=s(e);fetch(e.href,t)}})();const l=document.querySelector("#app");l.innerHTML=`
  <main class="shell">
    <header class="hero">
      <p class="eyebrow">IT Support Ticket</p>
      <h1>Application scaffold</h1>
      <p class="subtitle">
        The frontend shell is ready for the ticket workflow UI. Backend business logic will be implemented in the next step.
      </p>
    </header>

    <section class="panel">
      <h2>Planned workflow</h2>
      <ul>
        <li>Create ticket</li>
        <li>View ticket</li>
        <li>Assign technician</li>
        <li>Start work</li>
        <li>Resolve ticket</li>
      </ul>
    </section>
  </main>
`;
