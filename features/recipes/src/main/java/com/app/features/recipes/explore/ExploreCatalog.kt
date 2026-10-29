package com.app.features.recipes.explore

import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.Visibility

object ExploreCatalog {

    fun getCatalog(isEnglish: Boolean): List<Recipe> {
        return if (isEnglish) catalogEn else catalogEs
    }

    fun findRecipe(id: String, isEnglish: Boolean): Recipe? {
        val list = getCatalog(isEnglish)
        return list.firstOrNull { it.id == id } ?: catalogEs.firstOrNull { it.id == id }
    }

    val catalogEs = listOf(
        Recipe(
            id = "comm_1",
            name = "Tacos al Pastor",
            photoUrl = "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?auto=format&fit=crop&w=800&q=80",
            city = "Ciudad de México",
            country = "México",
            prepTimeMinutes = 45,
            attempts = 14,
            visibility = Visibility.PUBLIC,
            description = "Carne marinada con achiote y chiles, servida con piña asada, cebolla y cilantro fresco.",
            servings = 4,
            ingredients = listOf(
                Ingredient("500 g", "Carne de cerdo"),
                Ingredient("2 cdas.", "Pasta de achiote"),
                Ingredient("1/2 taza", "Jugo de naranja agria"),
                Ingredient("1 rodaja", "Piña asada"),
                Ingredient("12 un.", "Tortillas de maíz"),
                Ingredient("A gusto", "Cebolla y cilantro picados")
            ),
            steps = listOf(
                "Marinar el cerdo con el achiote y el jugo de naranja al menos 2 horas.",
                "Cocinar la carne en sartén bien caliente hasta que tome un dorado intenso.",
                "Picar la carne y servir sobre tortillas calientes con piña, cilantro y cebolla."
            ),
            latitude = 19.4326,
            longitude = -99.1332
        ),
        Recipe(
            id = "comm_2",
            name = "Paella Valenciana",
            photoUrl = "https://images.unsplash.com/photo-1534080564583-6be75777b70a?auto=format&fit=crop&w=800&q=80",
            city = "Valencia",
            country = "España",
            prepTimeMinutes = 55,
            attempts = 8,
            visibility = Visibility.PUBLIC,
            description = "Auténtica paella tradicional con arroz bomba, azafrán, pollo y judías verdes.",
            servings = 6,
            ingredients = listOf(
                Ingredient("400 g", "Arroz bomba"),
                Ingredient("500 g", "Pollo troceado"),
                Ingredient("150 g", "Judías verdes"),
                Ingredient("1 cdta.", "Hebras de azafrán"),
                Ingredient("1 litro", "Caldo de pollo"),
                Ingredient("3 cdas.", "Aceite de oliva virgen extra")
            ),
            steps = listOf(
                "Dorar bien el pollo en la paella con aceite de oliva.",
                "Agregar las judías verdes y sofreír unos minutos.",
                "Añadir el arroz, sofreírlo brevemente y verter el caldo caliente con azafrán.",
                "Cocinar 10 min a fuego vivo y 8 min a fuego suave hasta formar socarrat."
            ),
            latitude = 39.4699,
            longitude = -0.3763
        ),
        Recipe(
            id = "comm_3",
            name = "Croissant de Mantequilla",
            photoUrl = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=800&q=80",
            city = "París",
            country = "Francia",
            prepTimeMinutes = 90,
            attempts = 6,
            visibility = Visibility.PUBLIC,
            description = "Masa hojaldrada crujiente por fuera y tierna y hojaldrada por dentro.",
            servings = 8,
            ingredients = listOf(
                Ingredient("500 g", "Harina de trigo"),
                Ingredient("250 g", "Mantequilla seca fría"),
                Ingredient("10 g", "Levadura seca"),
                Ingredient("50 g", "Azúcar"),
                Ingredient("10 g", "Sal"),
                Ingredient("280 ml", "Leche fría")
            ),
            steps = listOf(
                "Amasar harina, levadura, azúcar, sal y leche hasta obtener una masa lisa.",
                "Laminar la mantequilla dentro de la masa realizando tres pliegues sencillos.",
                "Cortar triángulos, enrollar y fermentar 2 horas a 26°C.",
                "Pintar con huevo y hornear a 190°C durante 18 minutos."
            ),
            latitude = 48.8566,
            longitude = 2.3522
        ),
        Recipe(
            id = "comm_4",
            name = "Ramen Tonkotsu",
            photoUrl = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=800&q=80",
            city = "Fukuoka",
            country = "Japón",
            prepTimeMinutes = 35,
            attempts = 12,
            visibility = Visibility.PUBLIC,
            description = "Fideos artesanales en caldo cremoso con chashu de cerdo, huevo marinado y nori.",
            servings = 2,
            ingredients = listOf(
                Ingredient("200 g", "Fideos de ramen frescos"),
                Ingredient("600 ml", "Caldo concentrado tonkotsu"),
                Ingredient("4 fetas", "Chashu de cerdo"),
                Ingredient("2 un.", "Huevos ajitama marinados"),
                Ingredient("1 lámina", "Alga nori"),
                Ingredient("2 tallos", "Cebollino picado")
            ),
            steps = listOf(
                "Calentar el caldo tonkotsu hasta que esté muy caliente.",
                "Hervir los fideos durante 90 segundos y escurrir.",
                "Servir en tazón: base de fideos, caldo hirviendo, chashu, huevo y nori."
            ),
            latitude = 33.5902,
            longitude = 130.4017
        ),
        Recipe(
            id = "comm_5",
            name = "Pizza Margherita Napoletana",
            photoUrl = "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?auto=format&fit=crop&w=800&q=80",
            city = "Nápoles",
            country = "Italia",
            prepTimeMinutes = 30,
            attempts = 15,
            visibility = Visibility.PUBLIC,
            description = "Clásica pizza italiana con salsa de tomate San Marzano, mozzarella fior di latte y albahaca fresca.",
            servings = 3,
            ingredients = listOf(
                Ingredient("500 g", "Harina de fuerza 00"),
                Ingredient("325 ml", "Agua tibia"),
                Ingredient("3 g", "Levadura fresca"),
                Ingredient("200 g", "Tomates San Marzano triturados"),
                Ingredient("250 g", "Mozzarella de búfala"),
                Ingredient("Hojas", "Albahaca fresca")
            ),
            steps = listOf(
                "Amasar la harina, agua, levadura y sal. Dejar fermentar 24 horas en frío.",
                "Estirar la masa a mano dejando los bordes elevados (cornicione).",
                "Cubrir con tomate, mozzarella y un hilo de aceite de oliva.",
                "Hornear a máxima temperatura (idealmente 450°C) durante 90 segundos."
            ),
            latitude = 40.8518,
            longitude = 14.2681
        ),
        Recipe(
            id = "comm_6",
            name = "Asado Criollo",
            photoUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=800&q=80",
            city = "Buenos Aires",
            country = "Argentina",
            prepTimeMinutes = 120,
            attempts = 20,
            visibility = Visibility.PUBLIC,
            description = "Tira de asado y vacío a la parrilla con leña de quebracho y salsa chimichurri casera.",
            servings = 6,
            ingredients = listOf(
                Ingredient("1.5 kg", "Tira de asado"),
                Ingredient("1 kg", "Vacío de ternera"),
                Ingredient("4 un.", "Chorizos criollos"),
                Ingredient("A gusto", "Sal parrillera"),
                Ingredient("1 frasco", "Salsa chimichurri")
            ),
            steps = listOf(
                "Encender el fuego con leña y carbón hasta obtener brasas rojas y constantes.",
                "Salar la carne con sal gruesa y colocar en la parrilla del lado del hueso.",
                "Cocinar a fuego medio-bajo durante 60 minutos antes de dar vuelta.",
                "Servir bien caliente acompañado de chimichurri y ensalada."
            ),
            latitude = -34.6037,
            longitude = -58.3816
        ),
        Recipe(
            id = "comm_7",
            name = "Ceviche Peruano Tradicional",
            photoUrl = "https://images.unsplash.com/photo-1535399831218-d5bd36d1a6b3?auto=format&fit=crop&w=800&q=80",
            city = "Lima",
            country = "Perú",
            prepTimeMinutes = 25,
            attempts = 9,
            visibility = Visibility.PUBLIC,
            description = "Pescado fresco marinados al momento en jugo de limón sutil, ají limo y cebolla morada.",
            servings = 4,
            ingredients = listOf(
                Ingredient("600 g", "Pescado blanco fresco (corvina/lenguado)"),
                Ingredient("10 un.", "Limones sutiles exprimidos al instante"),
                Ingredient("1 un.", "Ají limo picado fino"),
                Ingredient("1 un.", "Cebolla morada en pluma"),
                Ingredient("1 rodaja", "Camote glaseado"),
                Ingredient("1 taza", "Choclo desgranado")
            ),
            steps = listOf(
                "Cortar el pescado limpio en cubos parejos de 2 cm.",
                "Sazonar con sal, ají limo y cilantro picado.",
                "Verter el jugo de limón recién exprimido y mezclar suavemente 1 minuto.",
                "Servir inmediatamente acompañado de camote, choclo y cebolla morada."
            ),
            latitude = -12.0464,
            longitude = -77.0428
        ),
        Recipe(
            id = "comm_8",
            name = "Pad Thai Tradicional",
            photoUrl = "https://images.unsplash.com/photo-1559847844-5315695dadae?auto=format&fit=crop&w=800&q=80",
            city = "Bangkok",
            country = "Tailandia",
            prepTimeMinutes = 30,
            attempts = 7,
            visibility = Visibility.PUBLIC,
            description = "Fideos de arroz salteados al wok con camarones, tofu, brotes de soja, maní y salsa de tamarindo.",
            servings = 2,
            ingredients = listOf(
                Ingredient("200 g", "Fideos de arroz planos"),
                Ingredient("150 g", "Camarones limpios"),
                Ingredient("100 g", "Tofu firme cortado en cubos"),
                Ingredient("2 cdas.", "Salsa de tamarindo"),
                Ingredient("1 cda.", "Salsa de pescado"),
                Ingredient("2 un.", "Huevos batidos"),
                Ingredient("1/2 taza", "Brotes de soja y maní tostado")
            ),
            steps = listOf(
                "Remojar los fideos de arroz en agua tibia hasta que estén flexibles.",
                "Saltear en el wok caliente el tofu y los camarones con un poco de aceite.",
                "Agregar los fideos, la salsa de tamarindo y pescado salteando enérgicamente.",
                "Empujar a un lado, cocinar el huevo revuelto y mezclar todo con brotes y maní."
            ),
            latitude = 13.7563,
            longitude = 100.5018
        ),
        Recipe(
            id = "comm_9",
            name = "Tiramisú Clásico Italiano",
            photoUrl = "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?auto=format&fit=crop&w=800&q=80",
            city = "Venecia",
            country = "Italia",
            prepTimeMinutes = 25,
            attempts = 11,
            visibility = Visibility.PUBLIC,
            description = "Postre artesanal con capas de bizcochos vainilla embebidos en café espresso y crema suave de queso mascarpone.",
            servings = 8,
            ingredients = listOf(
                Ingredient("500 g", "Queso mascarpone"),
                Ingredient("4 un.", "Huevos frescos separated"),
                Ingredient("100 g", "Azúcar fina"),
                Ingredient("300 ml", "Café espresso amargo frío"),
                Ingredient("24 un.", "Bizcochuelos / Vainillas (savoiardi)"),
                Ingredient("A gusto", "Cacao amargo en polvo")
            ),
            steps = listOf(
                "Batir las yemas con el azúcar hasta blanquear y sumar el mascarpone suavemente.",
                "Montar las claras a punto de nieve e incorporar con movimientos envolventes.",
                "Pasar los bizcochos rápidamente por el café e ir formando capas en una fuente.",
                "Cubrir con la crema de mascarpone y refrigerar mínimo 4 horas. Espolvorear cacao antes de servir."
            ),
            latitude = 45.4371,
            longitude = 12.3326
        ),
        Recipe(
            id = "comm_10",
            name = "Moussaka Griega",
            photoUrl = "https://images.unsplash.com/photo-1541544741938-0af808871cc0?auto=format&fit=crop&w=800&q=80",
            city = "Atenas",
            country = "Grecia",
            prepTimeMinutes = 60,
            attempts = 5,
            visibility = Visibility.PUBLIC,
            description = "Pastel al horno con capas de berenjenas gratinadas, carne picada especiada y salsa bechamel dorada.",
            servings = 6,
            ingredients = listOf(
                Ingredient("3 un.", "Berenjenas grandes en rodajas"),
                Ingredient("500 g", "Carne picada de cordero o ternera"),
                Ingredient("400 g", "Tomate triturado"),
                Ingredient("1 cdta.", "Canela molida"),
                Ingredient("500 ml", "Salsa bechamel rica"),
                Ingredient("100 g", "Queso graviera o parmesano rallado")
            ),
            steps = listOf(
                "Pincelar las rodajas de berenjena con aceite y dorar al horno o plancha.",
                "Cocinar la carne picada con cebolla, tomate, ajo y canela durante 20 minutos.",
                "Armar en fuente de horno: capa de berenjenas, relleno de carne, otra capa de berenjenas.",
                "Cubrir con la bechamel, espolvorear queso y hornear a 180°C durante 40 minutos hasta dorar."
            ),
            latitude = 37.9838,
            longitude = 23.7275
        )
    )

    val catalogEn = listOf(
        Recipe(
            id = "comm_1",
            name = "Pastor Tacos",
            photoUrl = "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?auto=format&fit=crop&w=800&q=80",
            city = "Mexico City",
            country = "Mexico",
            prepTimeMinutes = 45,
            attempts = 14,
            visibility = Visibility.PUBLIC,
            description = "Marinated pork with achiote and chiles, served with roasted pineapple and fresh cilantro.",
            servings = 4,
            ingredients = listOf(
                Ingredient("500 g", "Pork meat"),
                Ingredient("2 tbsp", "Achiote paste"),
                Ingredient("1/2 cup", "Sour orange juice"),
                Ingredient("1 slice", "Roasted pineapple"),
                Ingredient("12 pcs", "Corn tortillas"),
                Ingredient("To taste", "Chopped onion and cilantro")
            ),
            steps = listOf(
                "Marinate pork with achiote and orange juice for at least 2 hours.",
                "Cook meat in a hot skillet until deeply browned.",
                "Chop meat and serve on warm tortillas with pineapple, cilantro, and onion."
            ),
            latitude = 19.4326,
            longitude = -99.1332
        ),
        Recipe(
            id = "comm_2",
            name = "Valencian Paella",
            photoUrl = "https://images.unsplash.com/photo-1534080564583-6be75777b70a?auto=format&fit=crop&w=800&q=80",
            city = "Valencia",
            country = "Spain",
            prepTimeMinutes = 55,
            attempts = 8,
            visibility = Visibility.PUBLIC,
            description = "Authentic traditional paella with bomba rice, saffron, chicken, and green beans.",
            servings = 6,
            ingredients = listOf(
                Ingredient("400 g", "Bomba rice"),
                Ingredient("500 g", "Chopped chicken"),
                Ingredient("150 g", "Green beans"),
                Ingredient("1 tsp", "Saffron threads"),
                Ingredient("1 liter", "Chicken broth"),
                Ingredient("3 tbsp", "Extra virgin olive oil")
            ),
            steps = listOf(
                "Brown chicken well in paella pan with olive oil.",
                "Add green beans and sauté for a few minutes.",
                "Add rice, sauté briefly and pour hot broth with saffron.",
                "Cook 10 min on high heat and 8 min on low heat until socarrat forms."
            ),
            latitude = 39.4699,
            longitude = -0.3763
        ),
        Recipe(
            id = "comm_3",
            name = "Butter Croissant",
            photoUrl = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=800&q=80",
            city = "Paris",
            country = "France",
            prepTimeMinutes = 90,
            attempts = 6,
            visibility = Visibility.PUBLIC,
            description = "Flaky laminated pastry, crispy on the outside and tender on the inside.",
            servings = 8,
            ingredients = listOf(
                Ingredient("500 g", "Wheat flour"),
                Ingredient("250 g", "Cold dry butter"),
                Ingredient("10 g", "Dry yeast"),
                Ingredient("50 g", "Sugar"),
                Ingredient("10 g", "Salt"),
                Ingredient("280 ml", "Cold milk")
            ),
            steps = listOf(
                "Knead flour, yeast, sugar, salt, and milk into a smooth dough.",
                "Laminate butter inside dough with three simple folds.",
                "Cut triangles, roll up, and ferment for 2 hours at 26°C.",
                "Brush with egg and bake at 190°C for 18 minutes."
            ),
            latitude = 48.8566,
            longitude = 2.3522
        ),
        Recipe(
            id = "comm_4",
            name = "Tonkotsu Ramen",
            photoUrl = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=800&q=80",
            city = "Fukuoka",
            country = "Japan",
            prepTimeMinutes = 35,
            attempts = 12,
            visibility = Visibility.PUBLIC,
            description = "Artisanal noodles in creamy broth with pork chashu, marinated egg, and nori.",
            servings = 2,
            ingredients = listOf(
                Ingredient("200 g", "Fresh ramen noodles"),
                Ingredient("600 ml", "Concentrated tonkotsu broth"),
                Ingredient("4 slices", "Pork chashu"),
                Ingredient("2 pcs", "Marinated ajitama eggs"),
                Ingredient("1 sheet", "Nori seaweed"),
                Ingredient("2 stalks", "Chopped scallions")
            ),
            steps = listOf(
                "Heat tonkotsu broth until steaming hot.",
                "Boil noodles for 90 seconds and drain.",
                "Assemble in bowl: noodles, hot broth, chashu, egg, and nori."
            ),
            latitude = 33.5902,
            longitude = 130.4017
        ),
        Recipe(
            id = "comm_5",
            name = "Pizza Margherita Napoletana",
            photoUrl = "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?auto=format&fit=crop&w=800&q=80",
            city = "Naples",
            country = "Italy",
            prepTimeMinutes = 30,
            attempts = 15,
            visibility = Visibility.PUBLIC,
            description = "Classic Italian pizza with San Marzano tomato sauce, fior di latte mozzarella, and fresh basil.",
            servings = 3,
            ingredients = listOf(
                Ingredient("500 g", "Type 00 flour"),
                Ingredient("325 ml", "Warm water"),
                Ingredient("3 g", "Fresh yeast"),
                Ingredient("200 g", "Crushed San Marzano tomatoes"),
                Ingredient("250 g", "Buffalo mozzarella"),
                Ingredient("Leaves", "Fresh basil")
            ),
            steps = listOf(
                "Knead flour, water, yeast, and salt. Ferment for 24 hours in cold.",
                "Stretch dough by hand leaving raised crust edges.",
                "Top with tomato, mozzarella, and olive oil drizzle.",
                "Bake at maximum heat for 90 seconds."
            ),
            latitude = 40.8518,
            longitude = 14.2681
        ),
        Recipe(
            id = "comm_6",
            name = "Argentine Asado BBQ",
            photoUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=800&q=80",
            city = "Buenos Aires",
            country = "Argentina",
            prepTimeMinutes = 120,
            attempts = 20,
            visibility = Visibility.PUBLIC,
            description = "Grilled beef short ribs and flank steak with chimichurri sauce over wood embers.",
            servings = 6,
            ingredients = listOf(
                Ingredient("1.5 kg", "Beef short ribs"),
                Ingredient("1 kg", "Beef flank steak"),
                Ingredient("4 pcs", "Creole sausages"),
                Ingredient("To taste", "Coarse sea salt"),
                Ingredient("1 jar", "Chimichurri sauce")
            ),
            steps = listOf(
                "Light wood fire until steady red embers form.",
                "Salt meat generously and place on grill bone-side down.",
                "Cook over medium-low heat for 60 minutes before turning.",
                "Serve hot accompanied by chimichurri and salad."
            ),
            latitude = -34.6037,
            longitude = -58.3816
        ),
        Recipe(
            id = "comm_7",
            name = "Classic Peruvian Ceviche",
            photoUrl = "https://images.unsplash.com/photo-1535399831218-d5bd36d1a6b3?auto=format&fit=crop&w=800&q=80",
            city = "Lima",
            country = "Peru",
            prepTimeMinutes = 25,
            attempts = 9,
            visibility = Visibility.PUBLIC,
            description = "Fresh raw white fish cured in key lime juice with chili pepper and red onion.",
            servings = 4,
            ingredients = listOf(
                Ingredient("600 g", "Fresh white fish fillets"),
                Ingredient("10 pcs", "Key limes freshly squeezed"),
                Ingredient("1 pc", "Finely minced chili pepper"),
                Ingredient("1 pc", "Sliced red onion"),
                Ingredient("1 slice", "Glazed sweet potato"),
                Ingredient("1 cup", "Andean corn kernels")
            ),
            steps = listOf(
                "Cut fresh fish into even 2cm cubes.",
                "Season with salt, minced chili, and chopped cilantro.",
                "Pour freshly squeezed lime juice and stir gently for 1 minute.",
                "Serve immediately with sweet potato and corn."
            ),
            latitude = -12.0464,
            longitude = -77.0428
        ),
        Recipe(
            id = "comm_8",
            name = "Traditional Pad Thai",
            photoUrl = "https://images.unsplash.com/photo-1559847844-5315695dadae?auto=format&fit=crop&w=800&q=80",
            city = "Bangkok",
            country = "Thailand",
            prepTimeMinutes = 30,
            attempts = 7,
            visibility = Visibility.PUBLIC,
            description = "Stir-fried rice noodles with shrimp, tofu, bean sprouts, peanuts, and tamarind sauce.",
            servings = 2,
            ingredients = listOf(
                Ingredient("200 g", "Flat rice noodles"),
                Ingredient("150 g", "Clean shrimp"),
                Ingredient("100 g", "Firm tofu cubed"),
                Ingredient("2 tbsp", "Tamarind paste"),
                Ingredient("1 tbsp", "Fish sauce"),
                Ingredient("2 pcs", "Beaten eggs"),
                Ingredient("1/2 cup", "Bean sprouts and toasted peanuts")
            ),
            steps = listOf(
                "Soak rice noodles in warm water until pliable.",
                "Stir-fry tofu and shrimp in hot wok with oil.",
                "Add noodles, tamarind sauce, and fish sauce tossing quickly.",
                "Scramble eggs to one side and mix together with sprouts and peanuts."
            ),
            latitude = 13.7563,
            longitude = 100.5018
        ),
        Recipe(
            id = "comm_9",
            name = "Classic Italian Tiramisu",
            photoUrl = "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?auto=format&fit=crop&w=800&q=80",
            city = "Venice",
            country = "Italy",
            prepTimeMinutes = 25,
            attempts = 11,
            visibility = Visibility.PUBLIC,
            description = "Coffee-flavoured Italian dessert made of ladyfingers dipped in espresso and mascarpone cream.",
            servings = 8,
            ingredients = listOf(
                Ingredient("500 g", "Mascarpone cheese"),
                Ingredient("4 pcs", "Fresh egg yolks"),
                Ingredient("100 g", "Fine sugar"),
                Ingredient("300 ml", "Cold espresso coffee"),
                Ingredient("24 pcs", "Ladyfingers (savoiardi)"),
                Ingredient("To taste", "Unsweetened cocoa powder")
            ),
            steps = listOf(
                "Whip yolks with sugar until pale and fold in mascarpone.",
                "Whip egg whites to stiff peaks and fold in gently.",
                "Dip ladyfingers briefly in coffee and layer in dish.",
                "Top with mascarpone cream and chill 4 hours. Dust cocoa before serving."
            ),
            latitude = 45.4371,
            longitude = 12.3326
        ),
        Recipe(
            id = "comm_10",
            name = "Greek Moussaka",
            photoUrl = "https://images.unsplash.com/photo-1541544741938-0af808871cc0?auto=format&fit=crop&w=800&q=80",
            city = "Athens",
            country = "Greece",
            prepTimeMinutes = 60,
            attempts = 5,
            visibility = Visibility.PUBLIC,
            description = "Baked casserole layered with sauteed eggplant, spiced minced meat, and golden béchamel sauce.",
            servings = 6,
            ingredients = listOf(
                Ingredient("3 pcs", "Large eggplants sliced"),
                Ingredient("500 g", "Minced lamb or beef"),
                Ingredient("400 g", "Crushed tomatoes"),
                Ingredient("1 tsp", "Ground cinnamon"),
                Ingredient("500 ml", "Rich béchamel sauce"),
                Ingredient("100 g", "Grated parmesan cheese")
            ),
            steps = listOf(
                "Brush eggplant slices with oil and bake or grill until golden.",
                "Cook minced meat with onion, tomato, garlic, and cinnamon for 20 minutes.",
                "Layer in baking dish: eggplant, meat sauce, eggplant.",
                "Top with béchamel and cheese, bake at 180°C for 40 minutes until golden."
            ),
            latitude = 37.9838,
            longitude = 23.7275
        )
    )
}
