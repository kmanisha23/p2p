const API = '/api';
const inr = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' });

let materials = [];
let suppliers = [];
let requisitions = [];
let orders = [];
let receipts = [];

// ---------- helpers ----------
function $(id) {
    return document.getElementById(id);
}

function esc(value) {
    return String(value == null ? '' : value).replace(/[&<>"']/g, function (c) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
}

function badge(status) {
    const css = String(status).replace(/\s/g, '').toLowerCase();
    return '<span class="badge ' + css + '">' + esc(status) + '</span>';
}

function showMessage(text, isError) {
    const box = $('message');
    box.textContent = text;
    box.className = 'message ' + (isError ? 'error' : 'ok');
    box.style.display = 'block';
    setTimeout(function () { box.style.display = 'none'; }, 4000);
}

// GET, or POST with form fields (the Java 7 server reads form-encoded data)
async function api(path, fields) {
    const options = { method: 'GET' };
    if (fields) {
        options.method = 'POST';
        options.headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
        options.body = new URLSearchParams(fields).toString();
    }
    const response = await fetch(API + path, options);
    const data = await response.json().catch(function () { return null; });
    if (!response.ok) {
        throw new Error(data && data.error ? data.error : 'Request failed');
    }
    return data;
}

// ---------- loading + rendering ----------
async function loadDashboard() {
    const d = await api('/dashboard');
    $('d-materials').textContent = d.totalMaterials;
    $('d-suppliers').textContent = d.totalSuppliers;
    $('d-pending-prs').textContent = d.pendingRequisitions;
    $('d-open-pos').textContent = d.openOrders;
    $('d-inventory').textContent = d.totalInventory;
}

async function loadMaterials() {
    materials = await api('/materials');
    $('materials-body').innerHTML = materials.map(function (m) {
        return '<tr><td>' + esc(m.code) + '</td><td>' + esc(m.name) + '</td><td>' + esc(m.category) +
            '</td><td>' + inr.format(m.unitPrice) + '</td><td>' + esc(m.stockQuantity) + '</td></tr>';
    }).join('');
}

async function loadSuppliers() {
    suppliers = await api('/suppliers');
    $('suppliers-body').innerHTML = suppliers.map(function (s) {
        return '<tr><td>' + esc(s.code) + '</td><td>' + esc(s.name) + '</td><td>' + esc(s.contact) + '</td></tr>';
    }).join('');
}

async function loadRequisitions() {
    requisitions = await api('/purchase-requisitions');
    $('pr-body').innerHTML = requisitions.map(function (pr) {
        return '<tr><td>' + esc(pr.code) + '</td><td>' + esc(pr.department) + '</td><td>' + esc(pr.materialName) +
            '</td><td>' + esc(pr.quantity) + '</td><td>' + badge(pr.status) + '</td><td>' + esc(pr.date) + '</td></tr>';
    }).join('');
}

async function loadOrders() {
    orders = await api('/purchase-orders');
    $('po-body').innerHTML = orders.map(function (po) {
        return '<tr><td>' + esc(po.code) + '</td><td>' + esc(po.prCode) + '</td><td>' + esc(po.supplierName) +
            '</td><td>' + esc(po.materialName) + '</td><td>' + esc(po.quantity) +
            '</td><td>' + inr.format(po.unitPrice) + '</td><td>' + inr.format(po.totalAmount) +
            '</td><td>' + esc(po.receivedQuantity) + ' / ' + esc(po.quantity) +
            '</td><td>' + badge(po.status) + '</td><td>' + esc(po.date) + '</td></tr>';
    }).join('');
}

async function loadReceipts() {
    receipts = await api('/goods-receipts');
    $('gr-body').innerHTML = receipts.map(function (gr) {
        return '<tr><td>' + esc(gr.code) + '</td><td>' + esc(gr.poCode) + '</td><td>' + esc(gr.quantityOrdered) +
            '</td><td>' + esc(gr.quantityReceived) + '</td><td>' + esc(gr.date) + '</td><td>' + badge(gr.status) + '</td></tr>';
    }).join('');
}

// ---------- dropdowns ----------
function fillMaterialSelect() {
    $('pr-material').innerHTML = '<option value="">Select material</option>' + materials.map(function (m) {
        return '<option value="' + m.id + '">' + esc(m.name) + ' (' + inr.format(m.unitPrice) + ')</option>';
    }).join('');
}

function fillRequisitionSelect() {
    const pending = requisitions.filter(function (pr) { return pr.status === 'Pending'; });
    $('po-pr').innerHTML = '<option value="">Select pending PR</option>' + pending.map(function (pr) {
        return '<option value="' + pr.id + '">' + esc(pr.code) + ' - ' + esc(pr.materialName) + ' x ' +
            esc(pr.quantity) + ' (' + esc(pr.department) + ')</option>';
    }).join('');
}

function fillSupplierSelect() {
    $('po-supplier').innerHTML = '<option value="">Select supplier</option>' + suppliers.map(function (s) {
        return '<option value="' + s.id + '">' + esc(s.name) + '</option>';
    }).join('');
}

function fillOrderSelect() {
    const open = orders.filter(function (po) { return po.status !== 'Completed'; });
    $('gr-po').innerHTML = '<option value="">Select open PO</option>' + open.map(function (po) {
        return '<option value="' + po.id + '">' + esc(po.code) + ' - ' + esc(po.materialName) +
            ' (ordered ' + esc(po.quantity) + ', received ' + esc(po.receivedQuantity) + ')</option>';
    }).join('');
}

// ---------- page navigation ----------
const loaders = {
    dashboard: loadDashboard,
    materials: loadMaterials,
    suppliers: loadSuppliers,
    requisitions: async function () {
        await loadMaterials();
        await loadRequisitions();
        fillMaterialSelect();
    },
    orders: async function () {
        await loadSuppliers();
        await loadRequisitions();
        await loadOrders();
        fillRequisitionSelect();
        fillSupplierSelect();
    },
    receipts: async function () {
        await loadOrders();
        await loadReceipts();
        fillOrderSelect();
    }
};

async function showPage(name) {
    document.querySelectorAll('.page').forEach(function (p) { p.classList.remove('active'); });
    document.querySelectorAll('nav button').forEach(function (b) { b.classList.remove('active'); });
    $('page-' + name).classList.add('active');
    document.querySelector('nav button[data-page="' + name + '"]').classList.add('active');
    try {
        await loaders[name]();
    } catch (err) {
        showMessage(err.message, true);
    }
}

document.querySelectorAll('nav button').forEach(function (button) {
    button.addEventListener('click', function () { showPage(button.dataset.page); });
});

// ---------- forms ----------
function onSubmit(formId, handler) {
    $(formId).addEventListener('submit', async function (event) {
        event.preventDefault();
        try {
            await handler();
            event.target.reset();
        } catch (err) {
            showMessage(err.message, true);
        }
    });
}

onSubmit('material-form', async function () {
    await api('/materials', {
        name: $('m-name').value,
        category: $('m-category').value,
        unitPrice: $('m-price').value,
        stockQuantity: $('m-stock').value
    });
    showMessage('Material added', false);
    await loaders.materials();
});

onSubmit('supplier-form', async function () {
    await api('/suppliers', { name: $('s-name').value, contact: $('s-contact').value });
    showMessage('Supplier added', false);
    await loaders.suppliers();
});

onSubmit('pr-form', async function () {
    const pr = await api('/purchase-requisitions', {
        department: $('pr-department').value,
        materialId: $('pr-material').value,
        quantity: $('pr-quantity').value
    });
    showMessage('Purchase Requisition ' + pr.code + ' created', false);
    await loaders.requisitions();
});

onSubmit('po-form', async function () {
    const po = await api('/purchase-orders', {
        requisitionId: $('po-pr').value,
        supplierId: $('po-supplier').value
    });
    showMessage('Purchase Order ' + po.code + ' created. Total: ' + inr.format(po.totalAmount), false);
    await loaders.orders();
});

onSubmit('gr-form', async function () {
    const gr = await api('/goods-receipts', {
        purchaseOrderId: $('gr-po').value,
        quantityReceived: $('gr-quantity').value
    });
    showMessage('Goods Receipt ' + gr.code + ' recorded. PO is now: ' + gr.poStatus, false);
    await loaders.receipts();
});

// ---------- start ----------
showPage('dashboard');
