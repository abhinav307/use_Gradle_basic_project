let chartInstance = null;

// Currency formatter
const formatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
});

// Init
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('date').valueAsDate = new Date();
    loadDashboard();

    document.getElementById('addTxnForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        await submitTransaction();
    });
});

async function loadDashboard() {
    showLoader(true);
    try {
        const response = await fetch('/api/portfolio');
        const data = await response.json();
        
        // Update top metrics
        document.getElementById('totalValue').innerText = formatter.format(data.totalValue);
        document.getElementById('totalCost').innerText = formatter.format(data.totalCost);
        
        const roiEl = document.getElementById('totalRoi');
        roiEl.innerText = (data.totalRoi >= 0 ? '+' : '') + data.totalRoi.toFixed(2) + '%';
        roiEl.className = `text-3xl font-bold ${data.totalRoi >= 0 ? 'text-green-500' : 'text-red-500'}`;

        // Render Table
        const tbody = document.getElementById('assetsTableBody');
        tbody.innerHTML = '';
        
        data.assets.forEach(asset => {
            const tr = document.createElement('tr');
            tr.className = 'hover:bg-gray-50';
            
            const isProfit = asset.roi >= 0;
            const roiColor = isProfit ? 'text-green-600' : 'text-red-600';
            const roiIcon = isProfit ? '<i class="fa-solid fa-arrow-trend-up mr-1"></i>' : '<i class="fa-solid fa-arrow-trend-down mr-1"></i>';

            tr.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900">${asset.symbol}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${asset.type === 'CRYPTO' ? 'bg-orange-100 text-orange-800' : 'bg-blue-100 text-blue-800'}">
                        ${asset.type}
                    </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-right">${asset.totalQuantity.toFixed(4)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-right">${formatter.format(asset.costBasis)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-right font-medium">${formatter.format(asset.livePrice)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900 text-right">${formatter.format(asset.currentValue)}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-bold text-right ${roiColor}">
                    ${roiIcon} ${(asset.roi > -100 ? asset.roi.toFixed(2) : 0)}%
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Update Chart
        renderChart(data.assets);

    } catch (error) {
        console.error('Failed to load portfolio', error);
    } finally {
        showLoader(false);
    }
}

async function submitTransaction() {
    const payload = {
        symbol: document.getElementById('symbol').value.trim(),
        type: document.getElementById('type').value,
        quantity: parseFloat(document.getElementById('quantity').value),
        price: parseFloat(document.getElementById('price').value),
        date: document.getElementById('date').value
    };

    const msgEl = document.getElementById('formMsg');
    
    try {
        const response = await fetch('/api/transactions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            msgEl.textContent = 'Transaction added!';
            msgEl.className = 'mt-3 text-sm text-center text-green-600 block';
            document.getElementById('addTxnForm').reset();
            document.getElementById('date').valueAsDate = new Date();
            loadDashboard(); // Refresh data
        } else {
            const err = await response.json();
            msgEl.textContent = 'Error: ' + err.error;
            msgEl.className = 'mt-3 text-sm text-center text-red-600 block';
        }
    } catch (error) {
        msgEl.textContent = 'Network error.';
        msgEl.className = 'mt-3 text-sm text-center text-red-600 block';
    }

    setTimeout(() => { msgEl.classList.add('hidden'); }, 3000);
}

async function refreshData() {
    await fetch('/api/refresh', { method: 'POST' });
    loadDashboard();
}

function renderChart(assets) {
    const ctx = document.getElementById('allocationChart').getContext('2d');
    
    const labels = assets.map(a => a.symbol);
    const data = assets.map(a => a.currentValue);
    const colors = [
        '#4f46e5', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4'
    ];

    if (chartInstance) {
        chartInstance.destroy();
    }

    chartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors.slice(0, assets.length),
                borderWidth: 0,
                hoverOffset: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right' }
            },
            cutout: '70%'
        }
    });
}

function showLoader(show) {
    if (show) {
        document.getElementById('loader').classList.remove('hidden');
        document.getElementById('dashboard').classList.add('hidden');
    } else {
        document.getElementById('loader').classList.add('hidden');
        document.getElementById('dashboard').classList.remove('hidden');
    }
}
