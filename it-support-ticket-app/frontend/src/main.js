import './styles.css';

const app = document.querySelector('#app');

app.innerHTML = `
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
