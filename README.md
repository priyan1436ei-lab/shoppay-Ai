🏪 ShopPay AI

AI-Powered Digital Payment Tracking & Business Intelligence Platform

«ShopPay AI is a smart FinTech platform designed for small shop owners to track digital payments, manage expenses, monitor business performance, synchronize financial data with Google Sheets, and get AI-powered business insights from a single dashboard.»

---

📌 Table of Contents

- "Overview" (#-overview)
- "Problem Statement" (#-problem-statement)
- "Proposed Solution" (#-proposed-solution)
- "Objectives" (#-objectives)
- "Key Features" (#-key-features)
- "How It Works" (#-how-it-works)
- "Google Sheets Automation" (#-google-sheets-automation)
- "AI Features" (#-ai-features)
- "Dashboard" (#-dashboard)
- "Transaction Management" (#-transaction-management)
- "Expense Management" (#-expense-management)
- "Analytics" (#-analytics)
- "Anomaly Detection" (#-anomaly-detection)
- "Revenue Forecasting" (#-revenue-forecasting)
- "Database Architecture" (#-database-architecture)
- "System Architecture" (#-system-architecture)
- "Security" (#-security)
- "Technology Stack" (#-technology-stack)
- "Project Structure" (#-project-structure)
- "Installation" (#-installation)
- "Environment Variables" (#-environment-variables)
- "Workflow" (#-complete-workflow)
- "Hackathon Demo" (#-hackathon-demo-flow)
- "Future Enhancements" (#-future-enhancements)
- "Conclusion" (#-conclusion)

---

📖 Overview

Small businesses receive a large number of digital payments every day through UPI, cards, bank transfers and other payment methods.

However, many small shop owners still depend on:

- Manual calculations
- Payment app history
- Bank statements
- Excel/Google Sheets
- Paper records

This makes it difficult to understand:

- How much money was received today
- How much was spent
- Which payment method is most common
- Which day performs best
- Whether revenue is increasing or decreasing
- Whether a transaction looks unusual
- Whether the monthly target can be achieved

ShopPay AI solves this problem by bringing transaction management, analytics, AI and automated Google Sheets synchronization into one platform.

---

❗ Problem Statement

Small Shop Owners Cannot Easily Track and Understand Their Digital Income

A small shop owner may receive dozens or hundreds of digital payments every day.

Payment information can be scattered across different sources, making it difficult to maintain an accurate financial record.

Manual tracking can result in:

- Duplicate transactions
- Calculation mistakes
- Missing records
- Time-consuming bookkeeping
- Poor understanding of revenue trends
- Difficulty tracking expenses
- Difficulty predicting future income

Therefore, there is a need for a simple and intelligent platform that can automatically organize payment information and convert it into useful business insights.

---

💡 Proposed Solution

ShopPay AI provides a centralized financial dashboard for small businesses.

The platform allows users to:

1. Create a shop profile
2. Add transactions manually
3. Import transactions through CSV
4. Validate transaction data
5. Detect duplicate records
6. Store transactions securely
7. Track income and expenses
8. Analyze revenue trends
9. Synchronize data with Google Sheets
10. Ask questions through an AI assistant
11. Detect unusual transactions
12. Forecast future revenue
13. Track monthly targets
14. Generate business reports

---

🎯 Objectives

The main objectives of ShopPay AI are:

- Reduce manual financial tracking
- Centralize digital payment records
- Automatically calculate daily income
- Track business expenses
- Provide real-time business analytics
- Synchronize data with Google Sheets
- Provide AI-powered financial insights
- Detect unusual transaction patterns
- Forecast future revenue
- Help shop owners make better business decisions

---

🚀 Key Features

💳 1. Digital Payment Tracking

Track transactions from:

- UPI
- Card
- Bank Transfer
- Other digital payment sources

Each transaction contains:

Transaction ID
Amount
Date
Time
Payment Method
Status
Shop ID

---

📝 2. Manual Transaction Entry

Shop owners can manually enter:

Amount
Transaction ID
Payment Method
Date
Time
Status

Example:

Amount: ₹850
Method: UPI
Transaction ID: TX001
Status: Successful

After saving, the transaction is stored in the database and reflected on the dashboard.

---

📂 3. CSV Import

Users can upload transaction statements in CSV format.

Example:

transaction_id,amount,date,time,payment_method,status

TX001,250,2026-08-31,09:30,UPI,successful
TX002,500,2026-08-31,10:10,UPI,successful
TX003,1200,2026-08-31,11:30,CARD,successful
TX004,350,2026-08-31,13:20,UPI,successful
TX005,800,2026-08-31,15:00,BANK,successful

Import Pipeline

CSV Upload
    ↓
File Parser
    ↓
Data Validation
    ↓
Duplicate Detection
    ↓
Preview
    ↓
User Confirmation
    ↓
Database

---

🔍 4. Transaction Validation

Every transaction is validated before being stored.

The system checks:

- Transaction ID
- Amount
- Date
- Payment method
- Transaction status
- Required fields

Invalid transaction example:

Amount = -₹500

System response:

❌ Invalid Transaction

Amount must be greater than ₹0.

---

♻️ 5. Duplicate Detection

If the same transaction ID already exists:

TX001

and the user uploads it again, ShopPay AI detects the duplicate.

⚠️ Duplicate Transaction

TX001 already exists.

Action: Skip

This prevents incorrect revenue calculations.

---

💰 6. Income Management

The system automatically calculates:

- Today's income
- Weekly income
- Monthly income
- Total revenue
- Average transaction value
- Payment-method revenue

Example:

₹250
+ ₹500
+ ₹1,200
+ ₹350
+ ₹800
----------------
₹3,100

---

💸 7. Expense Management

Users can record business expenses such as:

- Rent
- Electricity
- Salary
- Inventory purchase
- Transport
- Maintenance
- Other expenses

Example:

Revenue       ₹3,24,600
Expenses      ₹2,18,400
-----------------------
Estimated Net ₹1,06,200

«Profit figures are estimates unless complete accounting data is available.»

---

📊 8. Business Dashboard

The dashboard provides a real-time overview.

Example:

Today's Income
₹12,850

Transactions
47

Average Transaction
₹273

Monthly Revenue
₹3,24,600

Dashboard sections:

- Revenue summary
- Transaction count
- Expense summary
- Net cash-flow estimate
- Revenue charts
- Payment-method breakdown
- AI insights
- Target progress
- Alerts

---

📈 9. Revenue Analytics

ShopPay AI provides:

7-Day Analysis

Monday      ₹8,500
Tuesday     ₹9,200
Wednesday   ₹7,800
Thursday    ₹11,400
Friday      ₹13,200
Saturday    ₹15,600
Sunday      ₹12,850

Users can switch between:

- 7 Days
- 30 Days
- 3 Months
- Custom Date Range

---

💳 10. Payment Method Analytics

The platform groups transactions by payment method.

Example:

UPI
₹7,200

CARD
₹3,400

BANK
₹2,250

The dashboard can visualize this using:

- Donut chart
- Bar chart
- Percentage indicators

---

🤖 AI Business Assistant

The AI assistant allows shop owners to ask questions using natural language.

Examples:

"How much did I earn this week?"

"Which day was my best?"

"Which payment method is used most?"

"Why did my revenue decrease?"

"How much do I need per day to reach my target?"

"What are my biggest expenses?"

---

🧠 Ask My Business

The main AI feature is Ask My Business.

Instead of navigating through multiple screens, the owner can simply ask:

«"Innaiku business epdi pochu?"»

The system analyzes actual business data and generates a summary.

Example:

«"Today revenue is ₹12,850 from 47 transactions. UPI contributed 56% of recorded digital revenue, and the strongest sales period was 6 PM–9 PM."»

AI Data Flow

User Question
      ↓
AI Intent Detection
      ↓
Identify Required Data
      ↓
Query User's Shop Data
      ↓
Analytics Calculation
      ↓
AI Response Generation
      ↓
Answer

The AI should not invent financial values.

---

🔮 Revenue Forecasting

ShopPay AI can estimate future revenue using historical data.

Forecast Pipeline

Historical Transactions
        ↓
Daily Revenue
        ↓
Trend Analysis
        ↓
Day-of-Week Pattern
        ↓
Forecast Model
        ↓
Estimated Revenue

Example:

Tomorrow's Estimated Revenue

₹11,500 – ₹13,000

Forecasts are clearly labeled as estimates and are not guaranteed outcomes.

---

🚨 Anomaly Detection

The system identifies transactions that significantly differ from historical patterns.

Example:

Typical transactions:

₹200
₹500
₹750
₹1,000
₹1,200

New transaction:

₹25,000

The application can show:

⚠️ UNUSUAL ACTIVITY

₹25,000

This transaction is significantly
higher than your typical transaction.

[Review]

The system should not automatically classify the transaction as fraud.

---

🎯 Monthly Target Tracking

Users can set a revenue target.

Example:

Monthly Target
₹4,00,000

Current Revenue
₹3,24,600

Progress
81%

Remaining
₹75,400

The application can calculate the required daily revenue to reach the target.

Example:

Remaining Target: ₹75,400
Days Remaining: 6

Required Daily Revenue:
≈ ₹12,567

---

🏥 Business Health Score

ShopPay AI can provide a simple business health indicator.

Example:

BUSINESS HEALTH

82 / 100

🟢 GOOD

The score can consider:

- Revenue growth
- Expense trend
- Target progress
- Transaction consistency
- Cash-flow trend
- Unusual activity

The system also explains why the score changed.

---

📦 Inventory Management

Optional inventory module:

Product     Stock
------------------
Rice        12 bags
Milk        20 packs
Biscuits    42 packs

Low-stock notification:

⚠️ Low Stock

Rice may run out soon.

Future versions can use sales history to estimate restocking needs.

---

📸 Receipt OCR

Shop owners can upload an expense receipt image.

Receipt Image
     ↓
OCR
     ↓
Extract Data
     ↓
Amount
Vendor
Date
Category
     ↓
Review
     ↓
Add Expense

Example:

Amount: ₹2,450
Vendor: ABC Wholesale
Category: Inventory
Date: 31-08-2026

---

🎤 Voice AI Assistant

Users can ask financial questions using voice.

Example:

«"Innaiku evlo income vandhurukku?"»

AI:

«"Innaiku ₹12,850 digital income vandhurukku."»

This feature improves accessibility for shop owners who prefer speaking over typing.

---

🔄 Google Sheets Automation

One of the major automation features is Smart Google Sheets Sync.

ShopPay AI can synchronize transaction and financial data with Google Sheets.

App → Google Sheets

ShopPay AI
     ↓
Validate Transaction
     ↓
Supabase Database
     ↓
Google Sheets API
     ↓
Add / Update Row

Example:

ShopPay AI receives:

TX101
₹1,500
UPI
Success

Google Sheet automatically receives:

Date| Transaction ID| Amount| Method| Status
31-08-2026| TX101| ₹1,500| UPI| Success

No manual copy-paste is required.

---

🔁 Google Sheets → App Sync

Two-way synchronization can also be implemented.

Google Sheets
      ↓
Automation
      ↓
Validate New Row
      ↓
Check Duplicate
      ↓
Supabase
      ↓
ShopPay Dashboard
      ↓
AI Analytics

If an owner adds:

TX099 | ₹2,500 | UPI | Success

to the configured Google Sheet, the automation can detect the new row and synchronize it to ShopPay AI.

---

📑 Google Sheets Structure

A single workbook can contain:

ShopPay AI Workbook
│
├── Transactions
├── Expenses
├── Daily Summary
├── Monthly Summary
└── AI Insights

Transactions

Date
Transaction ID
Amount
Payment Method
Status

Expenses

Date
Category
Description
Amount

Daily Summary

Date
Total Income
Total Expenses
Net Amount
Transaction Count

Monthly Summary

Month
Revenue
Expenses
Estimated Profit
Growth %
Target Progress

---

⚡ Automation Benefits

Google Sheets synchronization provides:

- Automatic record keeping
- Less manual data entry
- Easy spreadsheet access
- Backup-style reporting
- Easy sharing with an accountant
- Familiar interface for small businesses
- Continuous financial data synchronization

---

💡 Smart Business Recommendations

ShopPay AI can convert analytics into recommendations.

Example:

«"Saturday revenue is significantly higher than your weekday average. Consider reviewing inventory levels before Saturday."»

Another example:

«"Your recorded expenses increased this week while revenue remained stable. Review your largest expense categories."»

Recommendations should be based on actual available data.

---

🧮 What-If Business Simulator

Users can test hypothetical scenarios.

Example:

«"What if I increase my product price by 5%?"»

The system can generate a scenario estimate based on historical assumptions.

Current Revenue
₹3.20L

Scenario
Price +5%

Estimated Revenue
₹3.35L – ₹3.45L

The result must be clearly labeled as a simulation, not a guaranteed prediction.

---

📅 Cash-Flow Calendar

A calendar can display money flowing into and out of the business.

MON     +₹8K
TUE     +₹9K
WED     -₹3K
THU     +₹12K
FRI     -₹8K
SAT     +₹16K
SUN     +₹13K

This helps owners understand cash-flow patterns.

---

📄 Reports

ShopPay AI can generate:

- Daily Report
- Weekly Report
- Monthly Report
- Income Report
- Expense Report
- Transaction Report
- Business Summary

Possible export formats:

PDF
CSV
Google Sheets

---

🏗️ System Architecture

                    SHOP OWNER
                         │
                         ▼
                ┌─────────────────┐
                │  ShopPay AI UI  │
                │  Next.js App    │
                └────────┬────────┘
                         │
             ┌───────────┴───────────┐
             │                       │
             ▼                       ▼
      Transaction API            AI Assistant
             │                       │
             ▼                       ▼
       ┌────────────┐          ┌────────────┐
       │  Supabase  │          │ AI Service │
       │ PostgreSQL │          └─────┬──────┘
       └──────┬─────┘                │
              │                      │
              └──────────┬───────────┘
                         ▼
                 ANALYTICS ENGINE
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
       Revenue        Anomaly        Forecast
       Analytics      Detection      Engine
          │              │              │
          └──────────────┼──────────────┘
                         ▼
                    DASHBOARD
                         │
                         ▼
                 Google Sheets
                  Synchronization

---

🗄️ Database Architecture

Suggested tables:

"profiles"

id
name
email
created_at

"shops"

id
owner_id
shop_name
category
currency
monthly_target
created_at

"transactions"

id
shop_id
transaction_id
amount
payment_method
transaction_date
transaction_time
status
source
created_at

"expenses"

id
shop_id
amount
category
description
expense_date
created_at

"ai_insights"

id
shop_id
insight_type
content
created_at

"daily_metrics"

id
shop_id
date
revenue
expenses
transaction_count
average_transaction

---

🔐 Security

Because this is a FinTech-related application, security is critical.

Never store:

- UPI PIN
- Bank password
- OTP
- Card CVV
- Full card number

Security practices:

- Secure authentication
- Row Level Security
- Server-side validation
- API authentication
- Environment variables for secrets
- HTTPS
- Input validation
- Duplicate protection
- User-specific data access

Data Isolation

User A
 ↓
Shop A
 ↓
Transactions A

User B
 ↓
Shop B
 ↓
Transactions B

User A must never be able to access User B's transaction data.

---

🛠️ Technology Stack

Frontend

- Next.js
- React
- TypeScript
- Tailwind CSS
- shadcn/ui

Backend

- Next.js API / Server Actions
- Supabase

Database

- PostgreSQL

Authentication

- Supabase Auth

AI

- OpenAI API

Charts

- Recharts

Automation

- Google Sheets API
- Google Apps Script or automation workflow

Deployment

- Vercel
- Supabase

---

📁 Suggested Project Structure

shoppay-ai/
│
├── app/
│   ├── dashboard/
│   ├── transactions/
│   ├── expenses/
│   ├── analytics/
│   ├── assistant/
│   ├── reports/
│   └── settings/
│
├── components/
│   ├── dashboard/
│   ├── charts/
│   ├── transactions/
│   ├── expenses/
│   └── ai/
│
├── lib/
│   ├── supabase/
│   ├── analytics/
│   ├── ai/
│   └── google-sheets/
│
├── api/
│   ├── transactions/
│   ├── analytics/
│   ├── ai/
│   └── sheets/
│
├── public/
│
├── supabase/
│   └── migrations/
│
├── .env.local
├── package.json
└── README.md

---

⚙️ Installation

1. Clone Repository

git clone <your-repository-url>
cd shoppay-ai

2. Install Dependencies

npm install

3. Configure Environment Variables

Create:

.env.local

Add the required Supabase, AI and Google integration credentials.

4. Run Development Server

npm run dev

Open:

http://localhost:3000

---

🔑 Environment Variables

Example:

NEXT_PUBLIC_SUPABASE_URL=
NEXT_PUBLIC_SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=

OPENAI_API_KEY=

GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_SHEETS_ID=

«Never commit secret keys to GitHub.»

---

🔄 Complete Workflow

             USER LOGIN
                 ↓
            SHOP SETUP
                 ↓
       ADD / IMPORT PAYMENT
                 ↓
             VALIDATION
                 ↓
        DUPLICATE DETECTION
                 ↓
          SUPABASE DATABASE
                 ↓
       ┌─────────┼─────────┐
       ↓         ↓         ↓
   Dashboard  Analytics   AI
       ↓         ↓         ↓
       └─────────┼─────────┘
                 ↓
       Google Sheets Sync
                 ↓
          Reports / Alerts

---

🧪 Example End-to-End Scenario

A customer makes a ₹1,500 UPI payment.

Step 1

Transaction enters ShopPay AI.

TX101
₹1,500
UPI
Success

Step 2

System validates the transaction.

Step 3

System checks whether "TX101" already exists.

Step 4

Transaction is stored in Supabase.

Step 5

Dashboard updates:

Today's Income
+₹1,500

Step 6

Google Sheets synchronization runs.

Google Sheets
TX101 | ₹1,500 | UPI | Success

Step 7

Analytics update.

Step 8

AI can use the updated metrics.

The entire process reduces manual work.

---

🏆 Hackathon Demo Flow

Recommended demo sequence:

1. Login

Show ShopPay AI landing/login.

2. Shop Setup

Create:

Priyan Stores
Category: Grocery
Target: ₹4,00,000

3. Import Transactions

Upload a CSV.

4. Validation

Show:

- Valid transactions
- Duplicate transaction
- Invalid transaction

5. Dashboard

Show:

Revenue
Transactions
Average
Payment Breakdown

6. Analytics

Show revenue chart and payment-method analysis.

7. Expense

Add a business expense.

8. AI Assistant

Ask:

«"How much did I earn this week?"»

Then:

«"Which day was best?"»

9. Anomaly

Show an unusual high-value transaction.

10. Forecast

Show estimated future revenue.

11. Google Sheets Automation ⭐

Add/import a transaction.

Show that the corresponding Google Sheet row is automatically updated.

12. Report

Generate a monthly business report.

---

🌟 Why ShopPay AI Is Different

Traditional payment tracking applications mainly show:

«"You received ₹12,850."»

ShopPay AI goes further:

«"You received ₹12,850 from 47 transactions. Revenue is above your recent average, UPI is your most-used payment method, your strongest sales period is 6–9 PM, and your current pace suggests you are on track toward your monthly target."»

The platform transforms:

RAW PAYMENT DATA
        ↓
STRUCTURED DATA
        ↓
ANALYTICS
        ↓
AI INSIGHTS
        ↓
BUSINESS DECISIONS

---

🔮 Future Enhancements

Future versions can include:

- Official payment-provider integrations
- Automatic transaction synchronization
- WhatsApp business summaries
- Voice-based AI assistant
- Advanced inventory forecasting
- Product-level analytics
- Multi-store management
- Employee accounts
- Accountant access
- Advanced cash-flow forecasting
- Tax/accounting integrations
- More advanced ML models

---

🎯 Project Impact

ShopPay AI helps small businesses:

- Save bookkeeping time
- Reduce manual calculation
- Maintain organized records
- Understand revenue patterns
- Track expenses
- Monitor business target
