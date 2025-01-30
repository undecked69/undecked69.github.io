function calculateEOQ() {
    let D = parseFloat(document.getElementById("demand").value);   // Éves kereslet
    let S = parseFloat(document.getElementById("orderCost").value); // Rendelési költség
    let H = parseFloat(document.getElementById("holdingCost").value); // Tárolási költség
    let C = parseFloat(document.getElementById("productCost").value); // Termék ára
    let N = parseFloat(document.getElementById("daysPerYear").value); // Év napjai

    if (isNaN(D) || isNaN(S) || isNaN(H) || isNaN(C) || isNaN(N) || D <= 0 || S <= 0 || H <= 0 || C <= 0 || N <= 0) {
        document.getElementById("result").innerText = "Hibás bemenet! Adj meg pozitív számokat.";
        return;
    }

    // EOQ képlet: sqrt((2 * D * S) / H)
    let EOQ = Math.sqrt((2 * D * S) / H);

    // Rendelési költség: (D / EOQ) * S
    let totalOrderCost = (D / EOQ) * S;

    // Tárolási költség: (EOQ / 2) * H
    let totalHoldingCost = (EOQ / 2) * H;

    // Teljes költség: rendelési költség + tárolási költség + áruköltség (D * C)
    let totalCost = totalOrderCost + totalHoldingCost + (D * C);

    document.getElementById("result").innerText = `EOQ: ${EOQ.toFixed(2)} egység`;
    document.getElementById("totalCost").innerText = `Teljes költség: ${totalCost.toFixed(2)} HUF`;

    drawChart(EOQ, totalOrderCost, totalHoldingCost);
}

function drawChart(EOQ, orderCost, holdingCost) {
    let ctx = document.getElementById("eoqChart").getContext("2d");

    if (window.eoqChart) {
        window.eoqChart.destroy();
    }

    window.eoqChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ["EOQ", "Rendelési költség", "Tárolási költség"],
            datasets: [{
                label: "Értékek",
                data: [EOQ, orderCost, holdingCost],
                backgroundColor: ["#00a67e", "#ffcc00", "#ff5733"]
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}
