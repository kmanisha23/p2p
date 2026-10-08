# SAP MM-Inspired Purchase-to-Pay (Java 7 edition)

A small academic project inspired by the SAP MM procurement workflow.
It is NOT a real SAP system (no SAP GUI, ABAP, S/4HANA or Fiori).

Workflow: Purchase Requisition -> Purchase Order -> Goods Receipt -> Inventory Update

## What it uses
- Plain Java (runs on Java 7 and newer): built-in `com.sun.net.httpserver` web server
- HTML, CSS, vanilla JavaScript frontend
- Data kept in memory and saved to a file (`data.bin`), no database
- No Maven, no Spring Boot, no MySQL, no downloads

## Run it (Windows)
1. Double-click `run.bat`
2. Wait for "running", then open http://localhost:8080
3. Stop with Ctrl + C (or close the window)

From the VS Code terminal instead:
    java -cp bin sapp2p.Main

To start fresh with the sample data, stop the app and delete `data.bin`.

## Folder structure
    bin/        compiled classes (ready to run)
    src/sapp2p/ Java source code
        Main.java            starts the server
        ApiHandler.java      REST endpoints (controller)
        StaticHandler.java   serves the web page
        Store.java           business rules + saving (service/repository)
        Material, Supplier, Requisition, PurchaseOrder, GoodsReceipt   (entities)
    web/        index.html, css/style.css, js/app.js

## REST endpoints
    GET  /api/dashboard
    GET/POST /api/materials              name, category, unitPrice, stockQuantity
    GET/POST /api/suppliers              name, contact
    GET/POST /api/purchase-requisitions  department, materialId, quantity
    GET/POST /api/purchase-orders        requisitionId, supplierId
    GET/POST /api/goods-receipts         purchaseOrderId, quantityReceived

## Recompiling (only if you edit the code and have a JDK with javac)
    build-and-run.bat
