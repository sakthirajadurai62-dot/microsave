// =====================================================
// MicroSave Dashboard
// =====================================================

const API = {
    groups: "/api/groups",
    members: "/api/members",
    contributions: "/api/contributions",
    loans: "/api/loans",
    repayments: "/api/repayments"
};


// =====================================================
// PAGE NAVIGATION
// =====================================================

function showPage(pageId, clickedButton = null) {

    document.querySelectorAll(".page").forEach(page => {
        page.classList.remove("active-page");
    });

    const page = document.getElementById(pageId);

    if (page) {
        page.classList.add("active-page");
    }


    document.querySelectorAll(".nav-item").forEach(button => {
        button.classList.remove("active");
    });

    if (clickedButton) {
        clickedButton.classList.add("active");
    }


    const titles = {
        dashboard: "Dashboard",
        groups: "Groups",
        members: "Members",
        contributions: "Contributions",
        loans: "Loans",
        repayments: "Repayments"
    };


    const subtitles = {
        dashboard: "Self-Help Group Microfinance Management System",
        groups: "Manage Self-Help Groups",
        members: "Manage registered group members",
        contributions: "Track member savings and contributions",
        loans: "Manage member loans",
        repayments: "Track loan repayments"
    };


    document.getElementById("pageTitle").textContent =
        titles[pageId] || "MicroSave";

    document.getElementById("pageSubtitle").textContent =
        subtitles[pageId] || "";


    if (pageId === "dashboard") {
        loadDashboard();
    }

    if (pageId === "groups") {
        loadGroups();
    }

    if (pageId === "members") {
        loadMembers();
    }

    if (pageId === "contributions") {
        loadContributions();
    }

    if (pageId === "loans") {
        loadLoans();
    }

    if (pageId === "repayments") {
        loadRepayments();
    }
}


// =====================================================
// DASHBOARD
// =====================================================

async function loadDashboard() {

    try {

        const results = await Promise.allSettled([

            fetchData(API.groups),

            fetchData(API.members),

            fetchData(API.contributions),

            fetchData(API.loans)

        ]);


        const groups =
            results[0].status === "fulfilled"
                ? results[0].value
                : [];

        const members =
            results[1].status === "fulfilled"
                ? results[1].value
                : [];

        const contributions =
            results[2].status === "fulfilled"
                ? results[2].value
                : [];

        const loans =
            results[3].status === "fulfilled"
                ? results[3].value
                : [];


        document.getElementById("totalGroups").textContent =
            Array.isArray(groups) ? groups.length : 0;


        document.getElementById("totalMembers").textContent =
            Array.isArray(members) ? members.length : 0;


        const totalSavings =
            Array.isArray(contributions)
                ? contributions.reduce(
                    (sum, item) =>
                        sum + Number(item.amount || 0),
                    0
                )
                : 0;


        document.getElementById("totalSavings").textContent =
            formatCurrency(totalSavings);


        const activeLoans =
            Array.isArray(loans)
                ? loans.filter(
                    loan => loan.status === "ACTIVE"
                )
                : [];


        document.getElementById("activeLoans").textContent =
            activeLoans.length;


        const totalLoanAmount =
            Array.isArray(loans)
                ? loans.reduce(
                    (sum, loan) =>
                        sum + Number(
                            loan.principal ||
                            loan.amount ||
                            0
                        ),
                    0
                )
                : 0;


        const outstandingAmount =
            Array.isArray(loans)
                ? loans.reduce(
                    (sum, loan) =>
                        sum + Number(
                            loan.outstandingBalance || 0
                        ),
                    0
                )
                : 0;


        document.getElementById("totalLoanAmount").textContent =
            formatCurrency(totalLoanAmount);


        document.getElementById("outstandingAmount").textContent =
            formatCurrency(outstandingAmount);


        displayRecentLoans(loans);

    } catch (error) {

        console.error(
            "Dashboard loading error:",
            error
        );

    }
}


// =====================================================
// GROUPS
// =====================================================

async function loadGroups() {

    const container =
        document.getElementById("groupsContent");

    try {

        const groups =
            await fetchData(API.groups);


        if (!Array.isArray(groups) || groups.length === 0) {

            container.innerHTML =
                `<div class="empty">
                    No groups found.
                </div>`;

            return;
        }


        let html = `
            <div class="table-wrapper">

                <table>

                    <thead>

                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Location</th>
                            <th>Created Date</th>
                        </tr>

                    </thead>

                    <tbody>
        `;


        groups.forEach(group => {

            html += `
                <tr>

                    <td>${safe(group.id)}</td>

                    <td>
                        <strong>
                            ${safe(group.name)}
                        </strong>
                    </td>

                    <td>${safe(group.location)}</td>

                    <td>${safe(group.createdDate)}</td>

                </tr>
            `;

        });


        html += `
                    </tbody>

                </table>

            </div>
        `;


        container.innerHTML = html;

    } catch (error) {

        console.error(error);

        container.innerHTML =
            `<div class="empty">
                Unable to load groups.
            </div>`;
    }
}


// =====================================================
// MEMBERS
// =====================================================

async function loadMembers() {

    const container =
        document.getElementById("membersContent");

    try {

        const members =
            await fetchData(API.members);


        if (!Array.isArray(members) || members.length === 0) {

            container.innerHTML =
                `<div class="empty">
                    No members found.
                </div>`;

            return;
        }


        let html = `
            <div class="table-wrapper">

                <table>

                    <thead>

                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Phone</th>
                            <th>Group</th>
                            <th>Status</th>
                        </tr>

                    </thead>

                    <tbody>
        `;


        members.forEach(member => {

            const active =
                member.active === true;


            html += `
                <tr>

                    <td>${safe(member.id)}</td>

                    <td>
                        <strong>
                            ${safe(member.name)}
                        </strong>
                    </td>

                    <td>${safe(member.phone)}</td>

                    <td>${safe(member.groupId)}</td>

                    <td>
                        ${active ? "ACTIVE" : "INACTIVE"}
                    </td>

                </tr>
            `;

        });


        html += `
                    </tbody>

                </table>

            </div>
        `;


        container.innerHTML = html;

    } catch (error) {

        console.error(error);

        container.innerHTML =
            `<div class="empty">
                Unable to load members.
            </div>`;
    }
}


// =====================================================
// CONTRIBUTIONS
// =====================================================

async function loadContributions() {

    const container =
        document.getElementById("contributionsContent");

    try {

        const contributions =
            await fetchData(API.contributions);


        if (
            !Array.isArray(contributions) ||
            contributions.length === 0
        ) {

            container.innerHTML =
                `<div class="empty">
                    No contributions found.
                </div>`;

            return;
        }


        let html = `
            <div class="table-wrapper">

                <table>

                    <thead>

                        <tr>
                            <th>ID</th>
                            <th>Member</th>
                            <th>Amount</th>
                            <th>Date</th>
                        </tr>

                    </thead>

                    <tbody>
        `;


        contributions.forEach(item => {

            html += `
                <tr>

                    <td>${safe(item.id)}</td>

                    <td>${safe(item.memberId)}</td>

                    <td>
                        <strong>
                            ${formatCurrency(item.amount)}
                        </strong>
                    </td>

                    <td>
                        ${safe(item.contributionDate)}
                    </td>

                </tr>
            `;

        });


        html += `
                    </tbody>

                </table>

            </div>
        `;


        container.innerHTML = html;

    } catch (error) {

        console.error(error);

        container.innerHTML =
            `<div class="empty">
                Unable to load contributions.
            </div>`;
    }
}


// =====================================================
// LOANS
// =====================================================

async function loadLoans() {

    const container =
        document.getElementById("loansContent");

    try {

        const loans =
            await fetchData(API.loans);


        if (!Array.isArray(loans) || loans.length === 0) {

            container.innerHTML =
                `<div class="empty">
                    No loans found.
                </div>`;

            return;
        }


        let html = `
            <div class="table-wrapper">

                <table>

                    <thead>

                        <tr>
                            <th>ID</th>
                            <th>Member</th>
                            <th>Principal</th>
                            <th>Outstanding</th>
                            <th>Status</th>
                            <th>Date</th>
                        </tr>

                    </thead>

                    <tbody>
        `;


        loans.forEach(loan => {

            const principal =
                loan.principal ??
                loan.amount ??
                0;


            html += `
                <tr>

                    <td>${safe(loan.id)}</td>

                    <td>${safe(loan.memberId)}</td>

                    <td>
                        <strong>
                            ${formatCurrency(principal)}
                        </strong>
                    </td>

                    <td>
                        ${formatCurrency(
                            loan.outstandingBalance
                        )}
                    </td>

                    <td>${safe(loan.status)}</td>

                    <td>${safe(loan.disbursedDate)}</td>

                </tr>
            `;

        });


        html += `
                    </tbody>

                </table>

            </div>
        `;


        container.innerHTML = html;

    } catch (error) {

        console.error(error);

        container.innerHTML =
            `<div class="empty">
                Unable to load loans.
            </div>`;
    }
}


// =====================================================
// REPAYMENTS
// =====================================================

async function loadRepayments() {

    const container =
        document.getElementById("repaymentsContent");

    try {

        const repayments =
            await fetchData(API.repayments);


        if (
            !Array.isArray(repayments) ||
            repayments.length === 0
        ) {

            container.innerHTML =
                `<div class="empty">
                    No repayments found.
                </div>`;

            return;
        }


        let html = `
            <div class="table-wrapper">

                <table>

                    <thead>

                        <tr>
                            <th>ID</th>
                            <th>Loan</th>
                            <th>Amount</th>
                            <th>Date</th>
                        </tr>

                    </thead>

                    <tbody>
        `;


        repayments.forEach(item => {

            html += `
                <tr>

                    <td>${safe(item.id)}</td>

                    <td>${safe(item.loanId)}</td>

                    <td>
                        <strong>
                            ${formatCurrency(item.amount)}
                        </strong>
                    </td>

                    <td>
                        ${safe(item.repaymentDate)}
                    </td>

                </tr>
            `;

        });


        html += `
                    </tbody>

                </table>

            </div>
        `;


        container.innerHTML = html;

    } catch (error) {

        console.error(error);

        container.innerHTML =
            `<div class="empty">
                Unable to load repayments.
            </div>`;
    }
}


// =====================================================
// RECENT LOANS
// =====================================================

function displayRecentLoans(loans) {

    const container =
        document.getElementById("recentLoans");


    if (!Array.isArray(loans) || loans.length === 0) {

        container.innerHTML = `
            <tr>
                <td colspan="6" class="empty">
                    No loan records found.
                </td>
            </tr>
        `;

        return;
    }


    const recent =
        loans.slice(-5).reverse();


    container.innerHTML =
        recent.map(loan => {

            const principal =
                loan.principal ??
                loan.amount ??
                0;


            return `
                <tr>

                    <td>${safe(loan.id)}</td>

                    <td>${safe(loan.memberId)}</td>

                    <td>
                        ${formatCurrency(principal)}
                    </td>

                    <td>
                        ${formatCurrency(
                            loan.outstandingBalance
                        )}
                    </td>

                    <td>${safe(loan.status)}</td>

                    <td>${safe(loan.disbursedDate)}</td>

                </tr>
            `;

        }).join("");
}


// =====================================================
// FETCH
// =====================================================

async function fetchData(url) {

    const response =
        await fetch(url);


    if (!response.ok) {

        throw new Error(
            "API request failed: " +
            response.status
        );
    }


    return await response.json();
}


// =====================================================
// CURRENCY
// =====================================================

function formatCurrency(value) {

    const number =
        Number(value || 0);


    return new Intl.NumberFormat(
        "en-IN",
        {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 2
        }
    ).format(number);
}


// =====================================================
// SAFE TEXT
// =====================================================

function safe(value) {

    if (
        value === null ||
        value === undefined ||
        value === ""
    ) {
        return "-";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}


// =====================================================
// MESSAGE
// =====================================================

function showMessage(message) {

    const box =
        document.getElementById("messageBox");


    box.textContent = message;

    box.style.display = "block";


    setTimeout(() => {

        box.style.display = "none";

    }, 3000);
}


// =====================================================
// INITIAL LOAD
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        loadDashboard();

    }
);