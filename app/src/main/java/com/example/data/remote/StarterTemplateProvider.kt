package com.example.data.remote

import com.example.data.local.ProjectFileEntity
import com.example.data.model.ProjectType
import com.example.data.model.TechStack

object StarterTemplateProvider {

    fun generateStarterFiles(
        projectId: Long,
        projectName: String,
        projectType: ProjectType,
        techStack: TechStack
    ): List<ProjectFileEntity> {
        val files = mutableListOf<ProjectFileEntity>()

        when (projectType) {
            ProjectType.E_COMMERCE -> files.addAll(createEcommerceTemplate(projectId, projectName, techStack))
            ProjectType.PORTFOLIO -> files.addAll(createPortfolioTemplate(projectId, projectName, techStack))
            ProjectType.COMPANY -> files.addAll(createCompanyTemplate(projectId, projectName, techStack))
            ProjectType.RESTAURANT -> files.addAll(createRestaurantTemplate(projectId, projectName, techStack))
            ProjectType.DASHBOARD -> files.addAll(createDashboardTemplate(projectId, projectName, techStack))
            else -> files.addAll(createGenericTemplate(projectId, projectName, projectType, techStack))
        }

        return files
    }

    private fun createEcommerceTemplate(projectId: Long, name: String, tech: TechStack): List<ProjectFileEntity> {
        val indexHtml = """<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$name | متجر الإلكترونيات الذكي</title>
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        brand: {
                            50: '#f5f3ff',
                            500: '#8b5cf6',
                            600: '#7c3aed',
                            700: '#6d28d9',
                            900: '#4c1d95',
                        }
                    }
                }
            }
        }
    </script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="style.css">
</head>
<body class="bg-gray-950 text-gray-100 min-h-screen flex flex-col font-sans">

    <!-- Header & Navigation -->
    <header class="bg-gray-900/80 backdrop-blur-md sticky top-0 z-50 border-b border-gray-800">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
            <div class="flex items-center space-x-3 space-x-reverse">
                <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 to-indigo-500 flex items-center justify-center shadow-lg shadow-brand-500/30">
                    <i class="fa-solid fa-bolt text-white text-lg"></i>
                </div>
                <span class="text-xl font-bold tracking-tight text-white">$name</span>
            </div>

            <!-- Search Bar -->
            <div class="hidden md:flex flex-1 max-w-md mx-8">
                <div class="relative w-full">
                    <input type="text" id="searchInput" placeholder="ابحث عن أجهزة، هواتف، حواسيب..." 
                           class="w-full bg-gray-800/70 border border-gray-700 rounded-xl py-2 pr-10 pl-4 text-sm focus:outline-none focus:border-brand-500 text-gray-200">
                    <i class="fa-solid fa-magnifying-glass absolute right-3.5 top-3 text-gray-400 text-sm"></i>
                </div>
            </div>

            <!-- Actions -->
            <div class="flex items-center space-x-4 space-x-reverse">
                <button onclick="toggleCart()" class="relative p-2.5 rounded-xl bg-gray-800 hover:bg-gray-700 transition">
                    <i class="fa-solid fa-cart-shopping text-brand-400"></i>
                    <span id="cartCount" class="absolute -top-1.5 -right-1.5 bg-brand-600 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold">0</span>
                </button>
            </div>
        </div>
    </header>

    <!-- Hero Banner -->
    <section class="relative overflow-hidden bg-gradient-to-b from-gray-900 to-gray-950 py-16 px-4 sm:px-6 lg:px-8 border-b border-gray-800/60">
        <div class="max-w-7xl mx-auto text-center relative z-10">
            <span class="inline-block py-1 px-3 rounded-full bg-brand-500/10 border border-brand-500/30 text-brand-400 text-sm font-semibold mb-4">
                ✨ أحدث التكنولوجيا بأسعار استثنائية
            </span>
            <h1 class="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-white tracking-tight mb-6">
                عالمك الرقمي مع <span class="text-transparent bg-clip-text bg-gradient-to-r from-brand-400 to-indigo-400">$name</span>
            </h1>
            <p class="text-gray-400 text-lg max-w-2xl mx-auto mb-8">
                تسوق أحدث الأجهزة الذكية، الهواتف الذكية، والإكسسوارات الفاخرة مع ضمان حقيقي وشحن فوري لباب منزلك.
            </p>
        </div>
    </section>

    <!-- Category Filters -->
    <section class="max-w-7xl mx-auto px-4 py-8">
        <div class="flex items-center justify-center gap-3 overflow-x-auto pb-2" id="categoryFilter">
            <button onclick="filterCategory('all')" class="cat-btn active px-5 py-2 rounded-xl text-sm font-medium transition bg-brand-600 text-white">الكل</button>
            <button onclick="filterCategory('phones')" class="cat-btn px-5 py-2 rounded-xl text-sm font-medium transition bg-gray-800 text-gray-300 hover:bg-gray-700">هواتف ذكية</button>
            <button onclick="filterCategory('laptops')" class="cat-btn px-5 py-2 rounded-xl text-sm font-medium transition bg-gray-800 text-gray-300 hover:bg-gray-700">حواسيب محمولة</button>
            <button onclick="filterCategory('audio')" class="cat-btn px-5 py-2 rounded-xl text-sm font-medium transition bg-gray-800 text-gray-300 hover:bg-gray-700">سماعات وصوتيات</button>
            <button onclick="filterCategory('accessories')" class="cat-btn px-5 py-2 rounded-xl text-sm font-medium transition bg-gray-800 text-gray-300 hover:bg-gray-700">إكسسوارات</button>
        </div>
    </section>

    <!-- Product Grid -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 flex-1">
        <div id="productGrid" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            <!-- Dynamically populated by app.js -->
        </div>
    </main>

    <!-- Slide-over Cart Drawer -->
    <div id="cartDrawer" class="fixed inset-0 z-50 overflow-hidden hidden">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity" onclick="toggleCart()"></div>
        <div class="fixed inset-y-0 left-0 max-w-full flex">
            <div class="w-screen max-w-md bg-gray-900 border-r border-gray-800 p-6 flex flex-col justify-between shadow-2xl">
                <div>
                    <div class="flex items-center justify-between pb-4 border-b border-gray-800">
                        <h2 class="text-xl font-bold text-white flex items-center gap-2">
                            <i class="fa-solid fa-cart-shopping text-brand-400"></i> سلة المشتريات
                        </h2>
                        <button onclick="toggleCart()" class="text-gray-400 hover:text-white p-2">
                            <i class="fa-solid fa-xmark text-lg"></i>
                        </button>
                    </div>
                    <div id="cartItemsList" class="divide-y divide-gray-800 my-4 max-h-[60vh] overflow-y-auto">
                        <!-- Cart items injected here -->
                    </div>
                </div>
                <div class="pt-4 border-t border-gray-800">
                    <div class="flex justify-between items-center mb-4">
                        <span class="text-gray-400 font-medium">المجموع الإجمالي:</span>
                        <span id="cartTotal" class="text-2xl font-black text-white">$0.00</span>
                    </div>
                    <button onclick="checkout()" class="w-full bg-brand-600 hover:bg-brand-500 text-white font-bold py-3.5 rounded-xl transition shadow-lg shadow-brand-500/25">
                        إتمام الطلب الآن
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <footer class="bg-gray-900 border-t border-gray-800 mt-auto py-8">
        <div class="max-w-7xl mx-auto px-4 text-center text-sm text-gray-500">
            <p>© 2026 $name. تم إنشاؤه عبر منصة Dadix Web AI. جميع الحقوق محفوظة.</p>
        </div>
    </footer>

    <script src="app.js"></script>
</body>
</html>"""

        val styleCss = """/* Custom Styles for $name */
@import url('https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800&display=swap');

body {
    font-family: 'Cairo', sans-serif;
}

.cat-btn.active {
    background-color: #7c3aed !important;
    color: #ffffff !important;
}

.product-card {
    transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.product-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 28px -6px rgba(124, 58, 237, 0.25);
}"""

        val appJs = """// JavaScript Application Logic for $name
const products = [
    {
        id: 1,
        title: "هاتف Galaxy Ultra Max",
        category: "phones",
        price: 899,
        rating: 4.9,
        image: "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500&auto=format&fit=crop&q=60"
    },
    {
        id: 2,
        title: "حاسوب Pro Titanium X",
        category: "laptops",
        price: 1499,
        rating: 4.8,
        image: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&auto=format&fit=crop&q=60"
    },
    {
        id: 3,
        title: "سماعات Pro Wireless ANC",
        category: "audio",
        price: 199,
        rating: 4.7,
        image: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60"
    },
    {
        id: 4,
        title: "ساعة ذكية SmartWatch 6",
        category: "accessories",
        price: 249,
        rating: 4.6,
        image: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=60"
    },
    {
        id: 5,
        title: "شاشة ألعاب 4K OLED 144Hz",
        category: "laptops",
        price: 649,
        rating: 4.9,
        image: "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=60"
    },
    {
        id: 6,
        title: "شاحن سريع GaN 100W",
        category: "accessories",
        price: 49,
        rating: 4.5,
        image: "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&auto=format&fit=crop&q=60"
    }
];

let cart = [];
let currentCategory = 'all';

function renderProducts() {
    const grid = document.getElementById('productGrid');
    if (!grid) return;

    const filtered = currentCategory === 'all' 
        ? products 
        : products.filter(p => p.category === currentCategory);

    grid.innerHTML = filtered.map(p => `
        <div class="product-card bg-gray-900 border border-gray-800 rounded-2xl overflow-hidden flex flex-col justify-between">
            <div class="relative h-48 bg-gray-800 overflow-hidden">
                <img src="${'$'}{p.image}" alt="${'$'}{p.title}" class="w-full h-full object-cover">
                <span class="absolute top-3 left-3 bg-gray-900/80 backdrop-blur-sm text-yellow-400 text-xs px-2.5 py-1 rounded-full font-bold">
                    <i class="fa-solid fa-star text-xs"></i> ${'$'}{p.rating}
                </span>
            </div>
            <div class="p-5 flex-1 flex flex-col justify-between">
                <div>
                    <h3 class="text-base font-bold text-white mb-2">${'$'}{p.title}</h3>
                    <div class="text-xl font-extrabold text-brand-400 mb-4">${'$'}${'$'}{p.price}</div>
                </div>
                <button onclick="addToCart(${'$'}{p.id})" class="w-full py-2.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white font-medium flex items-center justify-center gap-2 transition">
                    <i class="fa-solid fa-cart-plus"></i> إضافة للسلة
                </button>
            </div>
        </div>
    `).join('');
}

function filterCategory(cat) {
    currentCategory = cat;
    document.querySelectorAll('.cat-btn').forEach(btn => btn.classList.remove('active', 'bg-brand-600', 'text-white'));
    event.target.classList.add('active', 'bg-brand-600', 'text-white');
    renderProducts();
}

function addToCart(productId) {
    const prod = products.find(p => p.id === productId);
    if (!prod) return;
    const existing = cart.find(c => c.id === productId);
    if (existing) {
        existing.qty += 1;
    } else {
        cart.push({ ...prod, qty: 1 });
    }
    updateCartUI();
    toggleCart(true);
}

function updateCartUI() {
    const countEl = document.getElementById('cartCount');
    const totalEl = document.getElementById('cartTotal');
    const listEl = document.getElementById('cartItemsList');

    const totalQty = cart.reduce((sum, item) => sum + item.qty, 0);
    const totalPrice = cart.reduce((sum, item) => sum + (item.price * item.qty), 0);

    if (countEl) countEl.innerText = totalQty;
    if (totalEl) totalEl.innerText = '$' + totalPrice.toFixed(2);

    if (listEl) {
        if (cart.length === 0) {
            listEl.innerHTML = '<p class="text-center text-gray-500 py-8">السلة فارغة حالياً</p>';
        } else {
            listEl.innerHTML = cart.map(item => `
                <div class="py-3 flex justify-between items-center">
                    <div>
                        <h4 class="text-white text-sm font-bold">${'$'}{item.title}</h4>
                        <span class="text-xs text-gray-400">${'$'}${'$'}{item.price} × ${'$'}{item.qty}</span>
                    </div>
                    <button onclick="removeFromCart(${'$'}{item.id})" class="text-red-400 hover:text-red-300 text-xs p-1">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </div>
            `).join('');
        }
    }
}

function removeFromCart(id) {
    cart = cart.filter(c => c.id !== id);
    updateCartUI();
}

function toggleCart(forceOpen = false) {
    const drawer = document.getElementById('cartDrawer');
    if (!drawer) return;
    if (forceOpen) {
        drawer.classList.remove('hidden');
    } else {
        drawer.classList.toggle('hidden');
    }
}

function checkout() {
    if (cart.length === 0) {
        alert("سلة المشتريات فارغة!");
        return;
    }
    alert("شكراً لك! تم تسجيل طلبك بنجاح. سيتم الاتصال بك لتأكيد الشحن.");
    cart = [];
    updateCartUI();
    toggleCart(false);
}

// Initial render
document.addEventListener('DOMContentLoaded', () => {
    renderProducts();
});
renderProducts();"""

        val packageJson = """{
  "name": "${name.lowercase().replace(" ", "-")}",
  "version": "1.0.0",
  "description": "Generated by Dadix Web AI",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "lucide": "^0.300.0"
  },
  "devDependencies": {
    "vite": "^5.0.0",
    "tailwindcss": "^3.4.0"
  }
}"""

        val readmeMd = """# $name

تم إنشاء هذا الموقع الإلكتروني عبر **Dadix Web AI**، منصة الذكاء الاصطناعي الاحترافية لبناء وتطوير المواقع.

## الميزات:
- تصميم تفاعلي حديث مع Tailwind CSS.
- كاتالوج للمنتجات مع تصفية ديناميكية وسريعة.
- سلة مشتريات تفاعلية فورية.
- متجاوب بالكامل مع الشاشات والهواتف (Responsive).

## التشغيل المحلي:
1. قم بفك الضغط عن المشروع.
2. افتح ملف `index.html` في متصفحك مباشرة أو شغّل `npm run dev`.
"""

        return listOf(
            ProjectFileEntity(projectId = projectId, path = "index.html", content = indexHtml),
            ProjectFileEntity(projectId = projectId, path = "style.css", content = styleCss),
            ProjectFileEntity(projectId = projectId, path = "app.js", content = appJs),
            ProjectFileEntity(projectId = projectId, path = "package.json", content = packageJson),
            ProjectFileEntity(projectId = projectId, path = "README.md", content = readmeMd)
        )
    }

    private fun createPortfolioTemplate(projectId: Long, name: String, tech: TechStack): List<ProjectFileEntity> {
        val indexHtml = """<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$name | معرض أعمال ومطور برمجيات</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="style.css">
</head>
<body class="bg-gray-950 text-gray-100 min-h-screen">
    <nav class="max-w-6xl mx-auto px-6 py-8 flex justify-between items-center">
        <div class="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
            <span class="w-3 h-3 rounded-full bg-purple-500 animate-pulse"></span> $name
        </div>
        <div class="flex gap-6 text-sm font-medium text-gray-400">
            <a href="#about" class="hover:text-purple-400 transition">نبذة</a>
            <a href="#projects" class="hover:text-purple-400 transition">المشاريع</a>
            <a href="#skills" class="hover:text-purple-400 transition">المهارات</a>
            <a href="#contact" class="hover:text-purple-400 transition">اتصل بي</a>
        </div>
    </nav>

    <header class="max-w-6xl mx-auto px-6 py-20 text-center">
        <span class="inline-block py-1.5 px-4 rounded-full bg-purple-500/10 border border-purple-500/20 text-purple-400 text-sm font-semibold mb-6">
            متاح للمشاريع والعمل الحر ✨
        </span>
        <h1 class="text-5xl sm:text-7xl font-extrabold text-white mb-6 tracking-tight">
            أبني تجارب ويب <span class="text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-cyan-400">استثنائية وسريعة</span>
        </h1>
        <p class="text-xl text-gray-400 max-w-2xl mx-auto mb-10">
            مطور واجهات ومصمم تفاعلي أساعد الشركات الناشئة ورواد الأعمال على تحويل أفكارهم إلى منتجات رقمية واقعية وجذابة.
        </p>
        <div class="flex justify-center gap-4">
            <a href="#projects" class="bg-purple-600 hover:bg-purple-500 text-white font-bold px-8 py-3.5 rounded-xl transition shadow-lg shadow-purple-600/30">
                استعرض أعمالي
            </a>
            <a href="#contact" class="bg-gray-800 hover:bg-gray-700 text-gray-200 font-bold px-8 py-3.5 rounded-xl transition border border-gray-700">
                تواصل معي
            </a>
        </div>
    </header>

    <script src="script.js"></script>
</body>
</html>"""

        val styleCss = """body { font-family: system-ui, -apple-system, sans-serif; }"""
        val scriptJs = """console.log("$name portfolio initialized successfully!");"""

        return listOf(
            ProjectFileEntity(projectId = projectId, path = "index.html", content = indexHtml),
            ProjectFileEntity(projectId = projectId, path = "style.css", content = styleCss),
            ProjectFileEntity(projectId = projectId, path = "script.js", content = scriptJs),
            ProjectFileEntity(projectId = projectId, path = "package.json", content = "{\n  \"name\": \"portfolio\",\n  \"version\": \"1.0.0\"\n}"),
            ProjectFileEntity(projectId = projectId, path = "README.md", content = "# $name Portfolio\nBuilt with Dadix Web AI.")
        )
    }

    private fun createCompanyTemplate(projectId: Long, name: String, tech: TechStack): List<ProjectFileEntity> {
        val indexHtml = """<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$name | حلول أعمال متطورة</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-950 text-slate-100">
    <div class="max-w-7xl mx-auto px-6 py-16 text-center">
        <h1 class="text-4xl sm:text-6xl font-bold mb-4 text-white">$name</h1>
        <p class="text-slate-400 text-lg max-w-2xl mx-auto">نقدم خدمات استشارية وتقنية متقدمة ترتقي بمشروعك لمستويات عالمية.</p>
    </div>
</body>
</html>"""
        return listOf(
            ProjectFileEntity(projectId = projectId, path = "index.html", content = indexHtml),
            ProjectFileEntity(projectId = projectId, path = "style.css", content = "/* Corporate Styles */"),
            ProjectFileEntity(projectId = projectId, path = "main.js", content = "console.log('Company site loaded');"),
            ProjectFileEntity(projectId = projectId, path = "package.json", content = "{\n  \"name\": \"corporate-site\",\n  \"version\": \"1.0.0\"\n}"),
            ProjectFileEntity(projectId = projectId, path = "README.md", content = "# $name Corporate Site")
        )
    }

    private fun createRestaurantTemplate(projectId: Long, name: String, tech: TechStack): List<ProjectFileEntity> {
        val indexHtml = """<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$name | مطعم وأطباق شرقية</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-stone-950 text-stone-100">
    <div class="max-w-6xl mx-auto px-6 py-16 text-center">
        <h1 class="text-5xl font-extrabold text-amber-500 mb-4">$name</h1>
        <p class="text-stone-400 text-lg">أشهى المأكولات وأرقى الأجواء مع خدمة حجز طاولات سريعة.</p>
    </div>
</body>
</html>"""
        return listOf(
            ProjectFileEntity(projectId = projectId, path = "index.html", content = indexHtml),
            ProjectFileEntity(projectId = projectId, path = "style.css", content = "/* Restaurant styles */"),
            ProjectFileEntity(projectId = projectId, path = "script.js", content = "console.log('Restaurant site initialized');"),
            ProjectFileEntity(projectId = projectId, path = "README.md", content = "# $name Restaurant")
        )
    }

    private fun createDashboardTemplate(projectId: Long, name: String, tech: TechStack): List<ProjectFileEntity> {
        val indexHtml = """<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$name | لوحة التحكم</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-950 text-gray-100 flex min-h-screen">
    <aside class="w-64 bg-gray-900 border-l border-gray-800 p-6 hidden md:block">
        <h2 class="text-xl font-bold text-purple-400 mb-8">$name</h2>
        <nav class="space-y-3 text-sm">
            <a href="#" class="block py-2 px-3 bg-purple-600 rounded-lg text-white font-medium">الرئيسية</a>
            <a href="#" class="block py-2 px-3 text-gray-400 hover:text-white">المستخدمين</a>
            <a href="#" class="block py-2 px-3 text-gray-400 hover:text-white">التقارير</a>
        </nav>
    </aside>
    <main class="flex-1 p-8">
        <h1 class="text-3xl font-bold mb-6">مؤشرات الأداء العامة</h1>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="p-6 bg-gray-900 rounded-2xl border border-gray-800">
                <span class="text-sm text-gray-400">إجمالي الزيارات</span>
                <div class="text-3xl font-black text-white mt-2">128,450</div>
            </div>
            <div class="p-6 bg-gray-900 rounded-2xl border border-gray-800">
                <span class="text-sm text-gray-400">المبيعات الشهرية</span>
                <div class="text-3xl font-black text-purple-400 mt-2">$42,900</div>
            </div>
            <div class="p-6 bg-gray-900 rounded-2xl border border-gray-800">
                <span class="text-sm text-gray-400">العملاء النشطون</span>
                <div class="text-3xl font-black text-emerald-400 mt-2">3,890</div>
            </div>
        </div>
    </main>
</body>
</html>"""
        return listOf(
            ProjectFileEntity(projectId = projectId, path = "index.html", content = indexHtml),
            ProjectFileEntity(projectId = projectId, path = "dashboard.js", content = "console.log('Dashboard loaded');"),
            ProjectFileEntity(projectId = projectId, path = "package.json", content = "{\n  \"name\": \"dashboard\",\n  \"version\": \"1.0.0\"\n}"),
            ProjectFileEntity(projectId = projectId, path = "README.md", content = "# $name Dashboard")
        )
    }

    private fun createGenericTemplate(projectId: Long, name: String, type: ProjectType, tech: TechStack): List<ProjectFileEntity> {
        val indexHtml = """<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$name</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-950 text-gray-100 min-h-screen flex items-center justify-center">
    <div class="text-center p-8 max-w-lg">
        <h1 class="text-4xl font-extrabold text-white mb-4">$name</h1>
        <p class="text-gray-400 mb-6">مشروع تم إنشاؤه بنجاح عبر منصة Dadix Web AI. ابدأ بتعديله عبر محرر الكود أو المحادثة الذكية.</p>
    </div>
</body>
</html>"""
        return listOf(
            ProjectFileEntity(projectId = projectId, path = "index.html", content = indexHtml),
            ProjectFileEntity(projectId = projectId, path = "style.css", content = "/* Custom Styles */"),
            ProjectFileEntity(projectId = projectId, path = "main.js", content = "console.log('$name ready');"),
            ProjectFileEntity(projectId = projectId, path = "README.md", content = "# $name\nBuilt with Dadix Web AI.")
        )
    }
}
