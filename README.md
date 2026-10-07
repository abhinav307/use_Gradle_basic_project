# Automated Financial Portfolio Tracker

A professional, web-based dashboard for tracking financial assets (Stocks and Cryptocurrencies) in real-time. Built with a modern Java 25 backend and a sleek Tailwind CSS frontend.

## 🚀 Features

- **Real-Time Market Data**: Fetches live prices for stocks (via Alpha Vantage) and cryptocurrencies (via CoinGecko).
- **Intelligent Caching**: Built-in 5-minute `PriceCache` to drastically reduce network calls and prevent hitting strict free-tier API rate limits.
- **Parallel Fetching**: Uses `CompletableFuture` to fetch all asset prices simultaneously, ensuring the dashboard loads instantly regardless of portfolio size.
- **Financial Analytics**: Automatically calculates Total Cost Basis, Current Value, and Return on Investment (ROI).
- **Embedded Database**: Uses an H2 embedded database to seamlessly persist all transactions locally.
- **Beautiful Web UI**: A modern, responsive dashboard built with Tailwind CSS and Chart.js for data visualization.

## 🛠️ Tech Stack

**Backend (Java 25 / Gradle):**
- **Javalin**: Lightweight framework serving REST APIs and static files.
- **H2 Database**: Fast, embedded SQL database for local persistence.
- **OkHttp**: For making HTTP REST calls to financial APIs.
- **Jackson**: For seamless JSON serialization and deserialization.
- **JUnit 5 & Mockito**: For robust unit testing.

**Frontend:**
- **Tailwind CSS**: Utility-first CSS framework (via CDN).
- **Chart.js**: For rendering the asset allocation doughnut chart.
- **Vanilla JavaScript**: Fetching API data and dynamically rendering the DOM.

## ⚙️ How to Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/abhinav307/use_Gradle_basic_project.git
   cd use_Gradle_basic_project/GradleDemo
   ```

2. **(Optional) Add Alpha Vantage API Key:**
   By default, the app uses a "demo" key which only supports the `IBM` stock ticker. To add real stocks, get a free key from [Alpha Vantage](https://www.alphavantage.co/) and set it as an environment variable:
   - **Windows:** `$env:ALPHA_VANTAGE_API_KEY="your_key_here"`
   - **Linux/Mac:** `export ALPHA_VANTAGE_API_KEY="your_key_here"`

3. **Start the Web Server:**
   ```bash
   ./gradlew run
   ```
   *(Note: The Gradle task will stay at `75% EXECUTING`. This is normal, as the web server runs continuously in the background.)*

4. **View the Dashboard:**
   Open your browser and navigate to: **[http://localhost:7070](http://localhost:7070)**

## 📚 Project Architecture

The codebase strictly follows Separation of Concerns (SoC) principles:
- **`com.tracker.model`**: Domain objects (`Transaction`, `Portfolio`, `MarketPrice`).
- **`com.tracker.client`**: HTTP API clients communicating with external market data providers.
- **`com.tracker.repository`**: Database connection management and SQL CRUD operations.
- **`com.tracker.service`**: Core business logic, mathematical ROI evaluations, and caching.
- **`com.tracker.Main`**: The web server orchestrator defining all `/api/*` REST endpoints.
- **`src/main/resources/public`**: The HTML, JS, and CSS for the frontend UI.
