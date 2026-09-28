const API = 'http://localhost:8080/api/v1/orders';

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
    symbol: document.querySelector('#symbol').value.trim().toUpperCase(),
    side: document.querySelector('#side').value,
    type: document.querySelector('#type').value,
    quantity: Number(document.querySelector('#quantity').value),
    price: document.querySelector('#price').value
      ? Number(document.querySelector('#price').value)
      : null
  };
}

async function loadOrders() {
  const response = await fetch(API);
  if (!response.ok) throw new Error(`API returned ${response.status}`);
  const orders = await response.json();
  ordersEl.replaceChildren();

  if (!orders.length) {
    const empty = document.createElement('p');
    empty.className = 'muted';
    empty.textContent = 'No orders loaded.';
    ordersEl.appendChild(empty);
    return;
  }

  for (const order of orders) {
    const article = document.createElement('article');
    article.className = 'order';

    const header = document.createElement('div');
    header.className = 'order-line';
    const title = document.createElement('strong');
    title.textContent = order.symbol;
    const direction = document.createElement('span');
    direction.textContent = `${order.side} ${order.type}`;
    header.append(title, direction);

    const details = document.createElement('div');
    details.className = 'order-line';
    const quantity = document.createElement('span');
    quantity.textContent = `${order.quantity} @ ${order.price ?? 'market'}`;
    const status = document.createElement('b');
    status.textContent = order.status;
    details.append(quantity, status);

    const meta = document.createElement('small');
    meta.textContent = `${order.clientOrderId} · ${new Date(order.createdAt).toLocaleString()}`;
    article.append(header, details, meta);
    ordersEl.appendChild(article);
  }
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
    if (!response.ok) {
      throw new Error(data.message || 'Order rejected');
    }

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
