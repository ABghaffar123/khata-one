package com.khatabook.app.ui.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * ═══════════════════════════════════════════════════════════════════
 * APP STRINGS — Complete multilingual string system
 * ═══════════════════════════════════════════════════════════════════
 *
 * Supports: English, Urdu (اردو), Roman Urdu
 * Usage: val strings = LocalStrings.current
 */

data class AppStrings(
    // ═══ Navigation ═══
    val navHome: String,
    val navCustomers: String,
    val navInventory: String,
    val navSuppliers: String,
    val navReports: String,
    val navSettings: String,

    // ═══ Home ═══
    val greetingMorning: String,
    val greetingAfternoon: String,
    val greetingEvening: String,
    val appName: String,
    val totalDues: String,
    val todayCollected: String,
    val thisMonth: String,
    val quickActions: String,
    val newEntry: String,
    val recentActivity: String,
    val noPendingDues: String,
    val noTransactions: String,
    val viewAll: String,

    // ═══ Customers ═══
    val customers: String,
    val addCustomer: String,
    val editCustomer: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val customerNotes: String,
    val saveCustomer: String,
    val searchCustomers: String,
    val noCustomers: String,

    // ═══ Transactions ═══
    val newTransaction: String,
    val selectCustomer: String,
    val transactionType: String,
    val credit: String,
    val payment: String,
    val cashSale: String,
    val purchase: String,
    val expense: String,
    val amount: String,
    val description: String,
    val dueDate: String,
    val optional: String,
    val saveTransaction: String,
    val transactionSaved: String,

    // ═══ Reports / Cash Book ═══
    val cashBookAndReports: String,
    val todaysCashSummary: String,
    val cashIn: String,
    val cashOut: String,
    val cashInHand: String,
    val openingCash: String,
    val closingCash: String,
    val cashSales: String,
    val paymentsReceived: String,
    val creditGiven: String,
    val expenses: String,
    val purchases: String,
    val paymentsToSuppliers: String,
    val incomeBreakdown: String,
    val businessOverview: String,
    val estimatedProfit: String,
    val stockValue: String,
    val potentialRevenue: String,
    val todayTransactions: String,
    val selectDateRange: String,
    val today: String,
    val thisWeek: String,
    val thisMonthShort: String,
    val custom: String,
    val dayEndReport: String,

    // ═══ Inventory ═══
    val inventory: String,
    val addProduct: String,
    val productName: String,
    val purchasePrice: String,
    val salePrice: String,
    val stockQuantity: String,
    val saveProduct: String,

    // ═══ Low Stock ═══
    val lowStock: String,
    val lowStockAlert: String,
    val productsNeedRestocking: String,
    val belowThreshold: String,
    val remaining: String,

    // ═══ Suppliers ═══
    val suppliers: String,
    val addSupplier: String,
    val supplierName: String,
    val supplierPhone: String,

    // ═══ Settings ═══
    val settings: String,
    val appearance: String,
    val language: String,
    val languageSettings: String,
    val theme: String,
    val security: String,
    val dataManagement: String,
    val backupCenter: String,
    val about: String,
    val securitySettings: String,
    val cashAccounts: String,

    // ═══ Language Settings ═══
    val chooseLanguage: String,
    val selectYourLanguage: String,
    val urduDesc: String,
    val englishDesc: String,
    val romanUrduDesc: String,
    val languageChanged: String,

    // ═══ SMS / WhatsApp Alerts ═══
    val sendAlert: String,
    val sendSms: String,
    val sendWhatsApp: String,
    val alertMessage: String,
    val alertPreview: String,
    val alertSent: String,
    val alertCancelled: String,

    // ═══ Common ═══
    val save: String,
    val cancel: String,
    val delete: String,
    val edit: String,
    val back: String,
    val continueText: String,
    val loading: String,
    val error: String,
    val success: String,
    val noData: String,
    val comingSoon: String,
    val rupees: String,

    // ═══ Empty States ═══
    val emptyCustomers: String,
    val emptyTransactions: String,
    val emptyProducts: String,
    val emptySuppliers: String,

    // ═══ Receipt / SMS Messages ═══
    val smsReceivedTemplate: String,
    val smsCreditTemplate: String,
    val whatsappReceivedTemplate: String,
    val whatsappCreditTemplate: String,

    // ═══ Invoice ═══
    val invoice: String,
    val invoiceNumber: String,
    val customer: String,
    val date: String,
    val phone: String,
    val item: String,
    val qty: String,
    val price: String,
    val total: String,
    val totalAmount: String,
    val itemsSubtotal: String,
    val adjustment: String,
    val thankYou: String,
    val generatedBy: String,
    val share: String,
    val print: String,

    // ═══ Barcode Scanner ═══
    val scanBarcode: String,
    val alignBarcode: String,
    val cameraPermissionRequired: String,
    val grantCameraAccess: String,
    val grantPermission: String,

    // ═══ Add Product (extended) ═══
    val barcodeOptional: String,
    val checkingBarcode: String,
    val unit: String,
    val salePriceRs: String,
    val purchasePriceRs: String,
    val lowStockAlertThreshold: String,
    val productAlreadyExists: String,
    val currentStock: String,
    val addStockInstead: String,
    val additionalQuantity: String,
    val updateStock: String,
    val createNew: String,

    // ═══ Reports (extended) ═══
    val exportPdf: String,
    val exportExcelCsv: String,

    // ═══ Supplier Detail (extended) ═══
    val recordPurchase: String,
    val givePayment: String,
    val recordTransaction: String,
    val amountRs: String,
    val descriptionOptional: String,

    // ═══ Customer Detail (extended) ═══
    val call: String,

    // ═══ Transaction Alert ═══
    val viewInvoice: String,

    // ═══ Daily Summary ═══
    val dailySummary: String,
    val dailySummaryReminder: String,
    val downloadPdf: String,
    val downloadImage: String,
    val downloadHistory: String,

    // ═══ Search & Filter ═══
    val searchTransactions: String,
    val allTypes: String,
    val transactions: String,
    val noTransactionsFound: String,

    // ═══ Search placeholders ═══
    val searchProducts: String,
    val searchSuppliers: String,
    val searchCustomersPlaceholder: String,

    // ═══ Extended Labels ═══
    val shopNameOptional: String,
    val addressOptional: String,
    val notesOptional: String,
    val saveSupplier: String,
    val type: String
)

val LocalStrings = staticCompositionLocalOf { EnglishStrings }

// ═══════════════════════════════════════════════════════════════════
// ENGLISH STRINGS
// ═══════════════════════════════════════════════════════════════════

val EnglishStrings = AppStrings(
    // Navigation
    navHome = "Home",
    navCustomers = "Customers",
    navInventory = "Inventory",
    navSuppliers = "Suppliers",
    navReports = "Reports",
    navSettings = "Settings",

    // Home
    greetingMorning = "Good Morning",
    greetingAfternoon = "Good Afternoon",
    greetingEvening = "Good Evening",
    appName = "Khata One",
    totalDues = "Total Dues",
    todayCollected = "Today",
    thisMonth = "This Month",
    quickActions = "Quick Actions",
    newEntry = "New Entry",
    recentActivity = "Recent Activity",
    noPendingDues = "No pending dues",
    noTransactions = "No transactions yet",
    viewAll = "View All",

    // Customers
    customers = "Customers",
    addCustomer = "Add Customer",
    editCustomer = "Edit Customer",
    customerName = "Customer Name",
    customerPhone = "Phone Number",
    customerAddress = "Address",
    customerNotes = "Notes",
    saveCustomer = "Save Customer",
    searchCustomers = "Search customers",
    noCustomers = "No customers yet",

    // Transactions
    newTransaction = "New Transaction",
    selectCustomer = "Select Customer",
    transactionType = "Transaction Type",
    credit = "Credit",
    payment = "Payment",
    cashSale = "Cash Sale",
    purchase = "Purchase",
    expense = "Expense",
    amount = "Amount",
    description = "Description",
    dueDate = "Due Date",
    optional = "Optional",
    saveTransaction = "Save Transaction",
    transactionSaved = "Transaction saved successfully",

    // Reports / Cash Book
    cashBookAndReports = "Cash Book & Reports",
    todaysCashSummary = "Today's Cash Summary",
    cashIn = "Cash In",
    cashOut = "Cash Out",
    cashInHand = "Cash in Hand",
    openingCash = "Opening Cash",
    closingCash = "Closing Cash",
    cashSales = "Cash Sales",
    paymentsReceived = "Payments Received",
    creditGiven = "Credit Given",
    expenses = "Expenses",
    purchases = "Purchases (Supplier)",
    paymentsToSuppliers = "Payments to Suppliers",
    incomeBreakdown = "Income Breakdown",
    businessOverview = "Business Overview",
    estimatedProfit = "Estimated Profit",
    stockValue = "Stock Value (Cost)",
    potentialRevenue = "Potential Revenue",
    todayTransactions = "Transactions",
    selectDateRange = "Select Range",
    today = "Today",
    thisWeek = "This Week",
    thisMonthShort = "This Month",
    custom = "Custom",
    dayEndReport = "Day-End Report",

    // Inventory
    inventory = "Inventory",
    addProduct = "Add Product",
    productName = "Product Name",
    purchasePrice = "Purchase Price",
    salePrice = "Sale Price",
    stockQuantity = "Stock Quantity",
    saveProduct = "Save Product",

    // Low Stock
    lowStock = "Low Stock",
    lowStockAlert = "Low Stock Alert",
    productsNeedRestocking = "products need restocking",
    belowThreshold = "These items are below their minimum stock threshold.",
    remaining = "remaining",

    // Suppliers
    suppliers = "Suppliers",
    addSupplier = "Add Supplier",
    supplierName = "Supplier Name",
    supplierPhone = "Phone Number",

    // Settings
    settings = "Settings",
    appearance = "Appearance",
    language = "Language",
    languageSettings = "Language Settings",
    theme = "Theme",
    security = "Security",
    dataManagement = "Data Management",
    backupCenter = "Backup Center",
    about = "About",
    securitySettings = "Security Settings",
    cashAccounts = "Cash Accounts",

    // Language Settings
    chooseLanguage = "Choose Your Language",
    selectYourLanguage = "Select your preferred language",
    urduDesc = "اردو - Full Urdu with Nastaliq script",
    englishDesc = "English - Default language",
    romanUrduDesc = "Roman Urdu - Urdu in Latin script",
    languageChanged = "Language changed successfully",

    // SMS / WhatsApp Alerts
    sendAlert = "Send Alert",
    sendSms = "Send SMS",
    sendWhatsApp = "Send WhatsApp",
    alertMessage = "Alert Message",
    alertPreview = "Message Preview",
    alertSent = "Alert sent successfully",
    alertCancelled = "Alert cancelled",

    // Common
    save = "Save",
    cancel = "Cancel",
    delete = "Delete",
    edit = "Edit",
    back = "Back",
    continueText = "Continue",
    loading = "Loading...",
    error = "Error",
    success = "Success",
    noData = "No data",
    comingSoon = "Coming Soon",
    rupees = "Rs",

    // Empty States
    emptyCustomers = "No customers yet.\nAdd your first customer to get started.",
    emptyTransactions = "No transactions yet.\nRecord your first transaction.",
    emptyProducts = "No products yet.\nAdd products to your inventory.",
    emptySuppliers = "No suppliers yet.\nAdd your first supplier.",

    // Receipt / SMS Messages
    smsReceivedTemplate = "Aap ka {shopName} par {amount} ka payment record ho gaya hai. Baaqi balance: {balance}. Shukriya!",
    smsCreditTemplate = "Aap ka {shopName} par {amount} ka credit record ho gaya hai. Baaqi balance: {balance}. Jald ada karein.",
    whatsappReceivedTemplate = "💰 Payment Receipt\n\n*{shopName}*\n\nCustomer: {customerName}\nAmount: {amount}\nType: Payment Received\nBalance: {balance}\nDate: {date}\n\nShukriya! 🙏",
    whatsappCreditTemplate = "📋 Credit Receipt\n\n*{shopName}*\n\nCustomer: {customerName}\nAmount: {amount}\nType: Credit Given\nBalance: {balance}\nDue Date: {dueDate}\nDate: {date}\n\nJald ada karein. Shukriya! 🙏",

    // Invoice
    invoice = "Invoice",
    invoiceNumber = "Invoice #",
    customer = "Customer",
    date = "Date",
    phone = "Phone",
    item = "Item",
    qty = "Qty",
    price = "Price",
    total = "Total",
    totalAmount = "Total Amount",
    itemsSubtotal = "Items Subtotal",
    adjustment = "Adjustment",
    thankYou = "Thank you for your business!",
    generatedBy = "Generated by Khata One",
    share = "Share",
    print = "Print",

    // Barcode Scanner
    scanBarcode = "Scan Barcode",
    alignBarcode = "Align barcode within the frame",
    cameraPermissionRequired = "Camera permission required",
    grantCameraAccess = "Please grant camera access to scan barcodes",
    grantPermission = "Grant Permission",

    // Add Product (extended)
    barcodeOptional = "Barcode (Optional)",
    checkingBarcode = "Checking barcode...",
    unit = "Unit",
    salePriceRs = "Sale Price (Rs)",
    purchasePriceRs = "Purchase Price (Rs)",
    lowStockAlertThreshold = "Low Stock Alert Threshold",
    productAlreadyExists = "Product Already Exists",
    currentStock = "Current Stock",
    addStockInstead = "Add stock to existing product instead of creating a duplicate?",
    additionalQuantity = "Additional Quantity",
    updateStock = "Update Stock",
    createNew = "Create New",

    // Reports (extended)
    exportPdf = "Export PDF",
    exportExcelCsv = "Export Excel (CSV)",

    // Supplier Detail (extended)
    recordPurchase = "Record Purchase",
    givePayment = "Give Payment",
    recordTransaction = "Record Transaction",
    amountRs = "Amount (Rs)",
    descriptionOptional = "Description (Optional)",

    // Customer Detail (extended)
    call = "Call",

    // Transaction Alert
    viewInvoice = "View Invoice",

    // Daily Summary
    dailySummary = "Daily Summary",
    dailySummaryReminder = "Daily business summary at 9 PM",
    downloadPdf = "Download as PDF",
    downloadImage = "Download as Image",
    downloadHistory = "Download History",

    // Search & Filter
    searchTransactions = "Search by customer or description...",
    allTypes = "All",
    transactions = "Transactions",
    noTransactionsFound = "No transactions found",

    // Search placeholders
    searchProducts = "Search products...",
    searchSuppliers = "Search suppliers...",
    searchCustomersPlaceholder = "Search customers...",

    // Extended Labels
    shopNameOptional = "Shop Name (Optional)",
    addressOptional = "Address (Optional)",
    notesOptional = "Notes (Optional)",
    saveSupplier = "Save Supplier",
    type = "Type"
)

// ═══════════════════════════════════════════════════════════════════
// URDU STRINGS (اردو)
// ═══════════════════════════════════════════════════════════════════

val UrduStrings = AppStrings(
    // Navigation
    navHome = "ہوم",
    navCustomers = "صارفین",
    navInventory = "انوینٹری",
    navSuppliers = "سپلائرز",
    navReports = "رپورٹس",
    navSettings = "ترتیبات",

    // Home
    greetingMorning = "صبح بخیر",
    greetingAfternoon = "دوپہر بخیر",
    greetingEvening = "شام بخیر",
    appName = "خ账簿 ون",
    totalDues = "کل واجبات",
    todayCollected = "آج",
    thisMonth = "اس مہینے",
    quickActions = "فوری اقدامات",
    newEntry = "نئی اندراج",
    recentActivity = "حالیہ سرگرمی",
    noPendingDues = "کوئی واجبات نہیں",
    noTransactions = "ابھی تک کوئی لین دین نہیں",
    viewAll = "سب دیکھیں",

    // Customers
    customers = "صارفین",
    addCustomer = "صارف شامل کریں",
    editCustomer = "صارف میں ترمیم",
    customerName = "صارف کا نام",
    customerPhone = "فون نمبر",
    customerAddress = "پتہ",
    customerNotes = "نوٹس",
    saveCustomer = "صارف محفوظ کریں",
    searchCustomers = "صارف تلاش کریں",
    noCustomers = "ابھی تک کوئی صارف نہیں",

    // Transactions
    newTransaction = "نیا لین دین",
    selectCustomer = "صارف منتخب کریں",
    transactionType = "لین دین کی قسم",
    credit = "ادھار",
    payment = "ادائیگی",
    cashSale = "نقد فروخت",
    purchase = "خریداری",
    expense = "اخراجات",
    amount = "رقم",
    description = "تفصیل",
    dueDate = "آخری تاریخ",
    optional = "اختیاری",
    saveTransaction = "لین دین محفوظ کریں",
    transactionSaved = "لین دین کامیابی سے محفوظ ہو گیا",

    // Reports / Cash Book
    cashBookAndReports = "خ账簿 اور رپورٹس",
    todaysCashSummary = "آج کا خلاصہ",
    cashIn = "ختم شدہ",
    cashOut = "خرچ شدہ",
    cashInHand = "ہاتھ میں نقد",
    openingCash = "ابتدائی نقد",
    closingCash = "اختتامی نقد",
    cashSales = "نقد فروخت",
    paymentsReceived = "وصول شدہ ادائیگیاں",
    creditGiven = "دیا گیا ادھار",
    expenses = "اخراجات",
    purchases = "خریداری (سپلائر)",
    paymentsToSuppliers = "سپلائرز کو ادائیگی",
    incomeBreakdown = "آمدنی کی تفصیل",
    businessOverview = "کاروبار کا خلاصہ",
    estimatedProfit = "تخمینہ منافع",
    stockValue = "اسٹاک ویلیو (لاگت)",
    potentialRevenue = "ممکنہ آمدنی",
    todayTransactions = "لین دین",
    selectDateRange = "ترینگ منتخب کریں",
    today = "آج",
    thisWeek = "اس ہفتے",
    thisMonthShort = "اس مہینے",
    custom = "حسب ضرورت",
    dayEndReport = "دن کے اختتام کی رپورٹ",

    // Inventory
    inventory = "انوینٹری",
    addProduct = "پروڈکٹ شامل کریں",
    productName = "پروڈکٹ کا نام",
    purchasePrice = "خریداری قیمت",
    salePrice = "فروخت قیمت",
    stockQuantity = "اسٹاک مقدار",
    saveProduct = "پروڈکٹ محفوظ کریں",

    // Low Stock
    lowStock = "کم اسٹاک",
    lowStockAlert = "کم اسٹاک الرٹ",
    productsNeedRestocking = "پروڈکٹس کو دوبارہ اسٹاک کرنے کی ضرورت ہے",
    belowThreshold = "یہ اشیاء ان کی کم از کم اسٹاک حد سے نیچے ہیں۔",
    remaining = "باقی",

    // Suppliers
    suppliers = "سپلائرز",
    addSupplier = "سپلائر شامل کریں",
    supplierName = "سپلائر کا نام",
    supplierPhone = "فون نمبر",

    // Settings
    settings = "ترتیبات",
    appearance = "ظاہری شکل",
    language = "زبان",
    languageSettings = "زبان کی ترتیبات",
    theme = "تھیم",
    security = "سیکیورٹی",
    dataManagement = "ڈیٹا مینجمنٹ",
    backupCenter = "بیک اپ سینٹر",
    about = "مارے میں",
    securitySettings = "سیکیورٹی کی ترتیبات",
    cashAccounts = "نقد اکاؤنٹس",

    // Language Settings
    chooseLanguage = "اپنی زبان منتخب کریں",
    selectYourLanguage = "اپنی پسندیدہ زبان منتخب کریں",
    urduDesc = "اردو - مکمل اردو نستعلیق",
    englishDesc = "English - Default language",
    romanUrduDesc = "Roman Urdu - لاطین لکھاؤ میں اردو",
    languageChanged = "زبان کامیابی سے بدل گئی",

    // SMS / WhatsApp Alerts
    sendAlert = "الرٹ بھیجیں",
    sendSms = "ایس ایم ایس بھیجیں",
    sendWhatsApp = "واٹس ایپ بھیجیں",
    alertMessage = "الرٹ پیغام",
    alertPreview = "پیغام کا پیش نظارہ",
    alertSent = "الرٹ کامیابی سے بھیجا گیا",
    alertCancelled = "الرٹ منسوخ",

    // Common
    save = "محفوظ کریں",
    cancel = "منسوخ کریں",
    delete = "حذف کریں",
    edit = "ترمیم کریں",
    back = "واپس",
    continueText = "جاری رہیں",
    loading = "لوڈ ہو رہا ہے...",
    error = "خرابی",
    success = "کامیابی",
    noData = "کوئی ڈیٹا نہیں",
    comingSoon = "جلد آ رہا ہے",
    rupees = "روپے",

    // Empty States
    emptyCustomers = "ابھی تک کوئی صارف نہیں۔\nاپنا پہلا صارف شامل کریں۔",
    emptyTransactions = "ابھی تک کوئی لین دین نہیں۔\nاپنا پہلا لین دین درج کریں۔",
    emptyProducts = "ابھی تک کوئی پروڈکٹ نہیں۔\nاپنی انوینٹری میں پروڈکٹ شامل کریں۔",
    emptySuppliers = "ابھی تک کوئی سپلائر نہیں۔\nاپنا پہلا سپلائر شامل کریں۔",

    // Receipt / SMS Messages
    smsReceivedTemplate = "{shopName} پر آپ کی {amount} کی ادائیگی موصول ہوئی۔ بقایا بلنس: {balance}۔ شکریہ!",
    smsCreditTemplate = "{shopName} پر آپ کا {amount} کا ادھار درج ہو گیا۔ بقایا بلنس: {balance}۔ جلد ادا کریں۔",
    whatsappReceivedTemplate = "💰 *ادائیگی کی رسید*\n\n*{shopName}*\n\nصارف: {customerName}\nرقم: {amount}\nقسم: ادائیگی وصول\nبقایا: {balance}\nتاریخ: {date}\n\nشکریہ! 🙏",
    whatsappCreditTemplate = "📋 *ادھار کی رسید*\n\n*{shopName}*\n\nصارف: {customerName}\nرقم: {amount}\nقسم: ادھار دیا گیا\nبقایا: {balance}\nآخری تاریخ: {dueDate}\nتاریخ: {date}\n\nجلد ادا کریں۔ شکریہ! 🙏",

    // Invoice
    invoice = "انوائس",
    invoiceNumber = "انوائس نمبر",
    customer = "صارف",
    date = "تاریخ",
    phone = "فون",
    item = "شے",
    qty = "مقدار",
    price = "قیمت",
    total = "کل",
    totalAmount = "کل رقم",
    itemsSubtotal = "اشیاء کی ذیلی کل",
    adjustment = "ترمیم",
    thankYou = "ہمارے کاروبار کے لیے شکریہ!",
    generatedBy = "خ账簿 ون نے بنایا",
    share = "شیئر کریں",
    print = "پرنٹ کریں",

    // Barcode Scanner
    scanBarcode = "بار کوڈ اسکین کریں",
    alignBarcode = "بار کوڈ کو فریم میں رکھیں",
    cameraPermissionRequired = "کیمرے کی اجازت درکار ہے",
    grantCameraAccess = "براہ کرم بار کوڈ اسکین کرنے کے لیے کیمرے کی اجازت دیں",
    grantPermission = "اجازت دیں",

    // Add Product (extended)
    barcodeOptional = "بار کوڈ (اختیاری)",
    checkingBarcode = "بار کوڈ چیک ہو رہا ہے...",
    unit = "اکائی",
    salePriceRs = "فروخت قیمت (روپے)",
    purchasePriceRs = "خریداری قیمت (روپے)",
    lowStockAlertThreshold = "کم اسٹاک الرٹ حد",
    productAlreadyExists = "پروڈکٹ پہلے سے موجود ہے",
    currentStock = "موجودہ اسٹاک",
    addStockInstead = "نقالہ بنانے کی بجائے موجودہ پروڈکٹ میں اسٹاک شامل کریں؟",
    additionalQuantity = "اضافی مقدار",
    updateStock = "اسٹاک اپ ڈیٹ کریں",
    createNew = "نیا بنائیں",

    // Reports (extended)
    exportPdf = "PDF ایکسپورٹ",
    exportExcelCsv = "ایکسل ایکسپورٹ (CSV)",

    // Supplier Detail (extended)
    recordPurchase = "خریداری درج کریں",
    givePayment = "ادائیگی دیں",
    recordTransaction = "لین دین درج کریں",
    amountRs = "رقم (روپے)",
    descriptionOptional = "تفصیل (اختیاری)",

    // Customer Detail (extended)
    call = "کال کریں",

    // Transaction Alert
    viewInvoice = "انوائس دیکھیں",

    // Daily Summary
    dailySummary = "روزانہ کا خلاصہ",
    dailySummaryReminder = "رات 9 بجے کا روزانہ کا خلاصہ",
    downloadPdf = "PDF ڈاؤن لوڈ کریں",
    downloadImage = "تصویر ڈاؤن لوڈ کریں",
    downloadHistory = "ڈاؤن لوڈ کی تاریخ",

    // Search & Filter
    searchTransactions = "صارف یا تفصیل سے تلاش کریں...",
    allTypes = "سب",
    transactions = "لین دین",
    noTransactionsFound = "کوئی لین دین نہیں ملا",

    // Search placeholders
    searchProducts = "پروڈکٹ تلاش کریں...",
    searchSuppliers = "سپلائر تلاش کریں...",
    searchCustomersPlaceholder = "صارف تلاش کریں...",

    // Extended Labels
    shopNameOptional = "دکان کا نام (اختیاری)",
    addressOptional = "پتہ (اختیاری)",
    notesOptional = "نوٹس (اختیاری)",
    saveSupplier = "سپلائر محفوظ کریں",
    type = "قسم"
)

// ═══════════════════════════════════════════════════════════════════
// ROMAN URDU STRINGS
// ═══════════════════════════════════════════════════════════════════

val RomanUrduStrings = AppStrings(
    // Navigation
    navHome = "Home",
    navCustomers = "Customers",
    navInventory = "Inventory",
    navSuppliers = "Suppliers",
    navReports = "Reports",
    navSettings = "Settings",

    // Home
    greetingMorning = "Subah Bakhair",
    greetingAfternoon = "Dopahar Bakhair",
    greetingEvening = "Shaam Bakhair",
    appName = "Khata One",
    totalDues = "Kul Wajaibaat",
    todayCollected = "Aaj",
    thisMonth = "Is Mahiney",
    quickActions = "Fori Iqdamat",
    newEntry = "Nayi Entry",
    recentActivity = "Haaliya Sargarmi",
    noPendingDues = "Koi wajaibaat nahi",
    noTransactions = "Abhi tak koi transaction nahi",
    viewAll = "Sab Dekhein",

    // Customers
    customers = "Customers",
    addCustomer = "Customer Shamil Karein",
    editCustomer = "Customer Mein Tarmeem",
    customerName = "Customer Ka Naam",
    customerPhone = "Phone Number",
    customerAddress = "Pata",
    customerNotes = "Notes",
    saveCustomer = "Customer Save Karein",
    searchCustomers = "Customer Dhoondein",
    noCustomers = "Abhi tak koi customer nahi",

    // Transactions
    newTransaction = "Naya Transaction",
    selectCustomer = "Customer Chunein",
    transactionType = "Transaction Ki Qisam",
    credit = "Udhaar",
    payment = "Adaigi",
    cashSale = "Naqd Farokht",
    purchase = "Khareedari",
    expense = "Kharche",
    amount = "Raqam",
    description = "Tafseel",
    dueDate = "Aakhri Tareekh",
    optional = "Ikhtiyari",
    saveTransaction = "Transaction Save Karein",
    transactionSaved = "Transaction kamyabi se save ho gaya",

    // Reports / Cash Book
    cashBookAndReports = "Cash Book Aur Reports",
    todaysCashSummary = "Aaj Ka Khulasa",
    cashIn = "Cash In",
    cashOut = "Cash Out",
    cashInHand = "Haath Mein Naqd",
    openingCash = "Ibtidai Naqd",
    closingCash = "Ikhtitami Naqd",
    cashSales = "Naqd Farokht",
    paymentsReceived = "Wasool Shuda Adaigiyan",
    creditGiven = "Diya Gaya Udhaar",
    expenses = "Kharche",
    purchases = "Khareedari (Supplier)",
    paymentsToSuppliers = "Suppliers Ko Adaigi",
    incomeBreakdown = "Aamdani Ki Tafseel",
    businessOverview = "Karobar Ka Khulasa",
    estimatedProfit = "Takhmina Munafa",
    stockValue = "Stock Value (Laagat)",
    potentialRevenue = "Mumkin Aamdani",
    todayTransactions = "Transactions",
    selectDateRange = "Range Chunein",
    today = "Aaj",
    thisWeek = "Is Haftay",
    thisMonthShort = "Is Mahiney",
    custom = "Hasb-e-Zaroorat",
    dayEndReport = "Din Ke Ikhtitam Ki Report",

    // Inventory
    inventory = "Inventory",
    addProduct = "Product Shamil Karein",
    productName = "Product Ka Naam",
    purchasePrice = "Khareedari Qeemat",
    salePrice = "Farokht Qeemat",
    stockQuantity = "Stock Miqdar",
    saveProduct = "Product Save Karein",

    // Low Stock
    lowStock = "Kam Stock",
    lowStockAlert = "Kam Stock Alert",
    productsNeedRestocking = "products ko dobara stock karne ki zaroorat hai",
    belowThreshold = "Yeh cheezein unki minimum stock had se neeche hain.",
    remaining = "baaqi",

    // Suppliers
    suppliers = "Suppliers",
    addSupplier = "Supplier Shamil Karein",
    supplierName = "Supplier Ka Naam",
    supplierPhone = "Phone Number",

    // Settings
    settings = "Settings",
    appearance = "Banaawat",
    language = "Zuban",
    languageSettings = "Zuban Ki Tarteep",
    theme = "Theme",
    security = "Security",
    dataManagement = "Data Management",
    backupCenter = "Backup Center",
    about = "Hamein Baarey Mein",
    securitySettings = "Security Ki Tarteep",
    cashAccounts = "Naqd Accounts",

    // Language Settings
    chooseLanguage = "Apni Zuban Chunein",
    selectYourLanguage = "Apni Pasandida Zuban Chunein",
    urduDesc = "Urdo - Mukammal Urdu Nastaleeq",
    englishDesc = "English - Default language",
    romanUrduDesc = "Roman Urdu - Latin likhaav mein Urdu",
    languageChanged = "Zuban kamyabi se badal gayi",

    // SMS / WhatsApp Alerts
    sendAlert = "Alert Bhejein",
    sendSms = "SMS Bhejein",
    sendWhatsApp = "WhatsApp Bhejein",
    alertMessage = "Alert Paigham",
    alertPreview = "Paigham Ka Preview",
    alertSent = "Alert kamyabi se bheja gaya",
    alertCancelled = "Alert mansookh",

    // Common
    save = "Save Karein",
    cancel = "Mansookh Karein",
    delete = "Delete Karein",
    edit = "Tarmeem",
    back = "Wapas",
    continueText = "Jaari Rahein",
    loading = "Load ho raha hai...",
    error = "Kharabi",
    success = "Kamyabi",
    noData = "Koi data nahi",
    comingSoon = "Jald aa raha hai",
    rupees = "Rs",

    // Empty States
    emptyCustomers = "Abhi tak koi customer nahi.\nPehla customer shamil karein.",
    emptyTransactions = "Abhi tak koi transaction nahi.\nPehla transaction darj karein.",
    emptyProducts = "Abhi tak koi product nahi.\nInventory mein product shamil karein.",
    emptySuppliers = "Abhi tak koi supplier nahi.\nPehla supplier shamil karein.",

    // Receipt / SMS Messages
    smsReceivedTemplate = "{shopName} par aap ki {amount} ki adaigi mili. Baqaya balance: {balance}. Shukriya!",
    smsCreditTemplate = "{shopName} par aap ka {amount} ka udhaar record ho gaya. Baqaya balance: {balance}. Jald ada karein.",
    whatsappReceivedTemplate = "💰 *Adaigi Ki Receipt*\n\n*{shopName}*\n\nCustomer: {customerName}\nRaqam: {amount}\nQisam: Adaigi Wasool\nBaqaya: {balance}\nTareekh: {date}\n\nShukriya! 🙏",
    whatsappCreditTemplate = "📋 *Udhaar Ki Receipt*\n\n*{shopName}*\n\nCustomer: {customerName}\nRaqam: {amount}\nQisam: Udhaar Diya Gaya\nBaqaya: {balance}\nAakhri Tareekh: {dueDate}\nTareekh: {date}\n\nJald ada karein. Shukriya! 🙏",

    // Invoice
    invoice = "Invoice",
    invoiceNumber = "Invoice #",
    customer = "Customer",
    date = "Tareekh",
    phone = "Phone",
    item = "Cheez",
    qty = "Miqdar",
    price = "Qeemat",
    total = "Kul",
    totalAmount = "Kul Raqam",
    itemsSubtotal = "Cheezein Ka Subtotal",
    adjustment = "Tarmeeem",
    thankYou = "Hamare karobar ke liye shukriya!",
    generatedBy = "Khata One ne banaya",
    share = "Share Karein",
    print = "Print Karein",

    // Barcode Scanner
    scanBarcode = "Barcode Scan Karein",
    alignBarcode = "Barcode ko frame mein rakhein",
    cameraPermissionRequired = "Camera ki ijaazat zaroori hai",
    grantCameraAccess = "Baraye meherbani barcode scan karne ke liye camera ki ijaazat dein",
    grantPermission = "Ijaazat Dein",

    // Add Product (extended)
    barcodeOptional = "Barcode (Ikhtiyari)",
    checkingBarcode = "Barcode check ho raha hai...",
    unit = "Ikaai",
    salePriceRs = "Farokht Qeemat (Rs)",
    purchasePriceRs = "Khareedari Qeemat (Rs)",
    lowStockAlertThreshold = "Kam Stock Alert Had",
    productAlreadyExists = "Product Pehle Se Mujood Hai",
    currentStock = "Maujuda Stock",
    addStockInstead = "Naqal banane ki bajaye maujuda product mein stock shamil karein?",
    additionalQuantity = "Izafi Miqdar",
    updateStock = "Stock Update Karein",
    createNew = "Naya Banayein",

    // Reports (extended)
    exportPdf = "PDF Export",
    exportExcelCsv = "Excel Export (CSV)",

    // Supplier Detail (extended)
    recordPurchase = "Khareedari Darj Karein",
    givePayment = "Adaigi Dein",
    recordTransaction = "Transaction Darj Karein",
    amountRs = "Raqam (Rs)",
    descriptionOptional = "Tafseel (Ikhtiyari)",

    // Customer Detail (extended)
    call = "Call",

    // Transaction Alert
    viewInvoice = "Invoice Dekhein",

    // Daily Summary
    dailySummary = "Rozana Ka Khulasa",
    dailySummaryReminder = "Raat 9 baje ka rozana khulasa",
    downloadPdf = "PDF Download Karein",
    downloadImage = "Tasveer Download Karein",
    downloadHistory = "Download Ki Tareekh",

    // Search & Filter
    searchTransactions = "Customer ya tafseel se dhoondein...",
    allTypes = "Sab",
    transactions = "Transactions",
    noTransactionsFound = "Koi transaction nahi mila",

    // Search placeholders
    searchProducts = "Product Dhoondein...",
    searchSuppliers = "Supplier Dhoondein...",
    searchCustomersPlaceholder = "Customer Dhoondein...",

    // Extended Labels
    shopNameOptional = "Dukaan Ka Naam (Ikhtiyari)",
    addressOptional = "Pata (Ikhtiyari)",
    notesOptional = "Notes (Ikhtiyari)",
    saveSupplier = "Supplier Save Karein",
    type = "Qisam"
)

// ═══════════════════════════════════════════════════════════════════
// HELPER
// ═══════════════════════════════════════════════════════════════════

fun getAppStrings(languageCode: String): AppStrings = when (languageCode) {
    "ur" -> UrduStrings
    "ur-roman" -> RomanUrduStrings
    else -> EnglishStrings
}
