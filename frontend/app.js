const API = 'http://localhost:8080/api/orders';

const form = document.querySelector('#order-form');
const ordersEl = document.querySelector('#orders');
const statusEl = document.querySelector('#status');

function setStatus(message, error = false) {
  statusEl.textContent = message;
  statusEl.classList.toggle('error', error);
}

function payload() {
  return {
    clientOrderId: document.querySelector('#clientOrderId').value.trim(),
    userId: document.querySelector('#userId').value.trim(),
    symbol: document.querySelector('#symbol').value.trim(),
    side: document.querySelector('#side').value,
    type: document.querySelector('#type').value,
    quantity: Number(document.querySelector('#quantity').value),
    price: document.querySelector('#price').value ? Number(document.querySelector('#price').value) : null
  };
}

async function loadOrders() {
  const response = await fetch(API);
  if (!response.ok) throw new Error(`API returned ${response.status}`);
  const orders = await response.json();
  ordersEl.innerHTML = orders.length ? orders.map(orderCard).join('') : '<p class="muted">No orders loaded.</p>';
}

function orderCard(order) {
  return `<article class="order">
    <div><strong>${order.symbol}</strong><span>${order.side} ${order.type}</span></div>
    <div><span>${order.quantity} @ ${order.price ?? 'market'}</span><b>${order.status}</b></div>
    <small>${order.clientOrderId} · ${new Date(order.createdAt).toLocaleString()}</small>
  </article>`;
}

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  setStatus('Submitting...');
  try {
    const response = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload())
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Order rejected');
    setStatus(`Accepted ${data.clientOrderId} as ${data.status}.`);
    await loadOrders();
  } catch (error) {
    setStatus(error.message, true);
  }
});

document.querySelector('#refresh').addEventListener('click', async () => {
  try {
    await loadOrders();
    setStatus('Orders refreshed.');
  } catch (error) {
    setStatus(`Cannot reach API: ${error.message}`, true);
  }
});

loadOrders().catch(() => setStatus('Start the Spring Boot API on port 8080 to load orders.', true));
