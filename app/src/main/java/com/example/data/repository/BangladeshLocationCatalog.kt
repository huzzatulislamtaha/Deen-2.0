package com.example.data.repository

import com.example.domain.model.BdDistrict
import com.example.domain.model.BdDivision
import com.example.domain.model.BdUpazila
import com.example.domain.model.CityLocation
import com.example.domain.model.CountryOption
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Official 8 Divisions & 64 Districts of Bangladesh with verified administrative HQ
 * coordinates (WGS84). All 64 districts use the national timezone Asia/Dhaka (UTC+06:00).
 */
object BangladeshLocationCatalog {

    const val BANGLADESH_COUNTRY_CODE = "BD"
    const val BANGLADESH_TIMEZONE_ID = "Asia/Dhaka"

    val countries: List<CountryOption> = listOf(
        CountryOption("BD", "Bangladesh", "বাংলাদেশ", "Asia/Dhaka"),
        CountryOption("SA", "Saudi Arabia", "সৌদি আরব", "Asia/Riyadh"),
        CountryOption("GB", "United Kingdom", "যুক্তরাজ্য", "Europe/London"),
        CountryOption("US", "United States", "যুক্তরাষ্ট্র", "America/New_York"),
        CountryOption("CA", "Canada", "কানাডা", "America/Toronto"),
        CountryOption("MY", "Malaysia", "মালয়েশিয়া", "Asia/Kuala_Lumpur"),
        CountryOption("ID", "Indonesia", "ইন্দোনেশিয়া", "Asia/Jakarta"),
        CountryOption("TR", "Turkey", "তুরস্ক", "Europe/Istanbul"),
        CountryOption("EG", "Egypt", "মিশর", "Africa/Cairo"),
        CountryOption("AE", "UAE", "সংযুক্ত আরব আমিরাত", "Asia/Dubai")
    )

    val divisions: List<BdDivision> = BdDivision.entries

    val districts: List<BdDistrict> = listOf(
        // 1. Dhaka Division (13 Districts)
        BdDistrict(
            id = "dist_dhaka",
            division = BdDivision.DHAKA,
            nameEn = "Dhaka",
            nameBn = "ঢাকা",
            latitude = 23.8103,
            longitude = 90.4125,
            upazilas = listOf(
                BdUpazila("upa_dhaka_dhanmondi", "dist_dhaka", "Dhanmondi", "ধানমন্ডি", 23.7461, 90.3742),
                BdUpazila("upa_dhaka_mirpur", "dist_dhaka", "Mirpur", "মিরপুর", 23.8223, 90.3654),
                BdUpazila("upa_dhaka_uttara", "dist_dhaka", "Uttara", "উত্তরা", 23.8759, 90.3795),
                BdUpazila("upa_dhaka_gulshan", "dist_dhaka", "Gulshan", "গুলশান", 23.7925, 90.4078),
                BdUpazila("upa_dhaka_savar", "dist_dhaka", "Savar", "সাভার", 23.8583, 90.2667),
                BdUpazila("upa_dhaka_keraniganj", "dist_dhaka", "Keraniganj", "কেরানীগঞ্জ", 23.6819, 90.3156)
            )
        ),
        BdDistrict("dist_faridpur", BdDivision.DHAKA, "Faridpur", "ফরিদপুর", 23.6071, 89.8429),
        BdDistrict(
            id = "dist_gazipur",
            division = BdDivision.DHAKA,
            nameEn = "Gazipur",
            nameBn = "গাজীপুর",
            latitude = 23.9999,
            longitude = 90.4203,
            upazilas = listOf(
                BdUpazila("upa_gazipur_sadar", "dist_gazipur", "Gazipur Sadar", "গাজীপুর সদর", 23.9999, 90.4203),
                BdUpazila("upa_gazipur_tongi", "dist_gazipur", "Tongi", "টঙ্গী", 23.8979, 90.4023),
                BdUpazila("upa_gazipur_sreepur", "dist_gazipur", "Sreepur", "শ্রীপুর", 24.1986, 90.4833)
            )
        ),
        BdDistrict("dist_gopalganj", BdDivision.DHAKA, "Gopalganj", "গোপালগঞ্জ", 23.0051, 89.8266),
        BdDistrict("dist_kishoreganj", BdDivision.DHAKA, "Kishoreganj", "কিশোরগঞ্জ", 24.4449, 90.7766),
        BdDistrict("dist_madaripur", BdDivision.DHAKA, "Madaripur", "মাদারীপুর", 23.1641, 90.1897),
        BdDistrict("dist_manikganj", BdDivision.DHAKA, "Manikganj", "মানিকগঞ্জ", 23.8617, 90.0003),
        BdDistrict("dist_munshiganj", BdDivision.DHAKA, "Munshiganj", "মুন্সিগঞ্জ", 23.5422, 90.5305),
        BdDistrict(
            id = "dist_narayanganj",
            division = BdDivision.DHAKA,
            nameEn = "Narayanganj",
            nameBn = "নারায়ণগঞ্জ",
            latitude = 23.6238,
            longitude = 90.5000,
            upazilas = listOf(
                BdUpazila("upa_narayanganj_sadar", "dist_narayanganj", "Narayanganj Sadar", "নারায়ণগঞ্জ সদর", 23.6238, 90.5000),
                BdUpazila("upa_narayanganj_sonargaon", "dist_narayanganj", "Sonargaon", "সোনারগাঁও", 23.6515, 90.6122)
            )
        ),
        BdDistrict("dist_narsingdi", BdDivision.DHAKA, "Narsingdi", "নরসিংদী", 23.9322, 90.7154),
        BdDistrict("dist_rajbari", BdDivision.DHAKA, "Rajbari", "রাজবাড়ী", 23.7574, 89.6445),
        BdDistrict("dist_shariatpur", BdDivision.DHAKA, "Shariatpur", "শরীয়তপুর", 23.2423, 90.4348),
        BdDistrict("dist_tangail", BdDivision.DHAKA, "Tangail", "টাঙ্গাইল", 24.2513, 89.9167),

        // 2. Chattogram Division (11 Districts)
        BdDistrict("dist_bandarban", BdDivision.CHATTOGRAM, "Bandarban", "বান্দরবান", 22.1953, 92.2184),
        BdDistrict("dist_brahmanbaria", BdDivision.CHATTOGRAM, "Brahmanbaria", "ব্রাহ্মণবাড়িয়া", 23.9571, 91.1117),
        BdDistrict("dist_chandpur", BdDivision.CHATTOGRAM, "Chandpur", "চাঁদপুর", 23.2333, 90.6667),
        BdDistrict(
            id = "dist_chattogram",
            division = BdDivision.CHATTOGRAM,
            nameEn = "Chattogram",
            nameBn = "চট্টগ্রাম",
            latitude = 22.3569,
            longitude = 91.7832,
            upazilas = listOf(
                BdUpazila("upa_ctg_agrabad", "dist_chattogram", "Agrabad / Double Mooring", "আগ্রাবাদ", 22.3247, 91.8126),
                BdUpazila("upa_ctg_hathazari", "dist_chattogram", "Hathazari", "হাটহাজারী", 22.5072, 91.8075),
                BdUpazila("upa_ctg_sitakunda", "dist_chattogram", "Sitakunda", "সীতাকুণ্ড", 22.6200, 91.6600)
            )
        ),
        BdDistrict(
            id = "dist_cumilla",
            division = BdDivision.CHATTOGRAM,
            nameEn = "Cumilla",
            nameBn = "কুমিল্লা",
            latitude = 23.4618,
            longitude = 91.1850,
            upazilas = listOf(
                BdUpazila("upa_cumilla_sadar", "dist_cumilla", "Cumilla Adarsha Sadar", "কুমিল্লা আদর্শ সদর", 23.4618, 91.1850),
                BdUpazila("upa_cumilla_daudkandi", "dist_cumilla", "Daudkandi", "দাউদকান্দি", 23.5333, 90.7167)
            )
        ),
        BdDistrict(
            id = "dist_coxs_bazar",
            division = BdDivision.CHATTOGRAM,
            nameEn = "Cox's Bazar",
            nameBn = "কক্সবাজার",
            latitude = 21.4272,
            longitude = 92.0058,
            upazilas = listOf(
                BdUpazila("upa_cox_sadar", "dist_coxs_bazar", "Cox's Bazar Sadar", "কক্সবাজার সদর", 21.4272, 92.0058),
                BdUpazila("upa_cox_teknaf", "dist_coxs_bazar", "Teknaf", "টেকনাফ", 20.8597, 92.2977)
            )
        ),
        BdDistrict("dist_feni", BdDivision.CHATTOGRAM, "Feni", "ফেনী", 23.0159, 91.3976),
        BdDistrict("dist_khagrachhari", BdDivision.CHATTOGRAM, "Khagrachhari", "খাগড়াছড়ি", 23.1193, 91.9847),
        BdDistrict("dist_lakshmipur", BdDivision.CHATTOGRAM, "Lakshmipur", "লক্ষ্মীপুর", 22.9447, 90.8282),
        BdDistrict("dist_noakhali", BdDivision.CHATTOGRAM, "Noakhali", "নোয়াখালী", 22.8696, 91.0994),
        BdDistrict("dist_rangamati", BdDivision.CHATTOGRAM, "Rangamati", "রাঙ্গামাটি", 22.6533, 92.1753),

        // 3. Rajshahi Division (8 Districts)
        BdDistrict("dist_bogura", BdDivision.RAJSHAHI, "Bogura", "বগুড়া", 24.8465, 89.3773),
        BdDistrict("dist_joypurhat", BdDivision.RAJSHAHI, "Joypurhat", "জয়পুরহাট", 25.0968, 89.0227),
        BdDistrict("dist_naogaon", BdDivision.RAJSHAHI, "Naogaon", "নওগাঁ", 24.7936, 88.9318),
        BdDistrict("dist_natore", BdDivision.RAJSHAHI, "Natore", "নাটোর", 24.4206, 89.0003),
        BdDistrict("dist_chapainawabganj", BdDivision.RAJSHAHI, "Chapainawabganj", "চাঁপাইনবাবগঞ্জ", 24.5965, 88.2775),
        BdDistrict("dist_pabna", BdDivision.RAJSHAHI, "Pabna", "পাবনা", 24.0063, 89.2372),
        BdDistrict(
            id = "dist_rajshahi",
            division = BdDivision.RAJSHAHI,
            nameEn = "Rajshahi",
            nameBn = "রাজশাহী",
            latitude = 24.3745,
            longitude = 88.6042,
            upazilas = listOf(
                BdUpazila("upa_raj_boalia", "dist_rajshahi", "Boalia (Rajshahi City)", "বোয়ালিয়া", 24.3745, 88.6042),
                BdUpazila("upa_raj_paba", "dist_rajshahi", "Paba", "পবা", 24.4422, 88.6347)
            )
        ),
        BdDistrict("dist_sirajganj", BdDivision.RAJSHAHI, "Sirajganj", "সিরাজগঞ্জ", 24.4534, 89.7007),

        // 4. Khulna Division (10 Districts)
        BdDistrict("dist_bagerhat", BdDivision.KHULNA, "Bagerhat", "বাগেরহাট", 22.6516, 89.7859),
        BdDistrict("dist_chuadanga", BdDivision.KHULNA, "Chuadanga", "চুয়াডাঙ্গা", 23.6402, 88.8418),
        BdDistrict("dist_jashore", BdDivision.KHULNA, "Jashore", "যশোর", 23.1664, 89.2081),
        BdDistrict("dist_jhenaidah", BdDivision.KHULNA, "Jhenaidah", "ঝিনাইদহ", 23.5450, 89.1726),
        BdDistrict(
            id = "dist_khulna",
            division = BdDivision.KHULNA,
            nameEn = "Khulna",
            nameBn = "খুলনা",
            latitude = 22.8456,
            longitude = 89.5403,
            upazilas = listOf(
                BdUpazila("upa_khulna_sadar", "dist_khulna", "Khulna Sadar", "খুলনা সদর", 22.8456, 89.5403),
                BdUpazila("upa_khulna_dumuria", "dist_khulna", "Dumuria", "ডুমুরিয়া", 22.8083, 89.4250)
            )
        ),
        BdDistrict("dist_kushtia", BdDivision.KHULNA, "Kushtia", "কুষ্টিয়া", 23.9013, 89.1204),
        BdDistrict("dist_magura", BdDivision.KHULNA, "Magura", "মাগুরা", 23.4873, 89.4198),
        BdDistrict("dist_meherpur", BdDivision.KHULNA, "Meherpur", "মেহেরপুর", 23.7622, 88.6318),
        BdDistrict("dist_narail", BdDivision.KHULNA, "Narail", "নড়াইল", 23.1725, 89.5126),
        BdDistrict("dist_satkhira", BdDivision.KHULNA, "Satkhira", "সাতক্ষীরা", 22.7185, 89.0705),

        // 5. Barishal Division (6 Districts)
        BdDistrict("dist_barguna", BdDivision.BARISHAL, "Barguna", "বরগুনা", 22.1592, 90.1256),
        BdDistrict("dist_barishal", BdDivision.BARISHAL, "Barishal", "বরিশাল", 22.7010, 90.3535),
        BdDistrict("dist_bhola", BdDivision.BARISHAL, "Bhola", "ভোলা", 22.6859, 90.6482),
        BdDistrict("dist_jhalokati", BdDivision.BARISHAL, "Jhalokati", "ঝালকাঠি", 22.6406, 90.2015),
        BdDistrict("dist_patuakhali", BdDivision.BARISHAL, "Patuakhali", "পটুয়াখালী", 22.3596, 90.3299),
        BdDistrict("dist_pirojpur", BdDivision.BARISHAL, "Pirojpur", "পিরোজপুর", 22.5841, 89.9720),

        // 6. Sylhet Division (4 Districts)
        BdDistrict("dist_habiganj", BdDivision.SYLHET, "Habiganj", "হবিগঞ্জ", 24.3749, 91.4155),
        BdDistrict("dist_moulvibazar", BdDivision.SYLHET, "Moulvibazar", "মৌলভীবাজার", 24.4829, 91.7774),
        BdDistrict("dist_sunamganj", BdDivision.SYLHET, "Sunamganj", "সুনামগঞ্জ", 25.0658, 91.3950),
        BdDistrict(
            id = "dist_sylhet",
            division = BdDivision.SYLHET,
            nameEn = "Sylhet",
            nameBn = "সিলেট",
            latitude = 24.8949,
            longitude = 91.8687,
            upazilas = listOf(
                BdUpazila("upa_sylhet_sadar", "dist_sylhet", "Sylhet Sadar", "সিলেট সদর", 24.8949, 91.8687),
                BdUpazila("upa_sylhet_beanibazar", "dist_sylhet", "Beanibazar", "বিয়ানীবাজার", 24.8225, 92.1636)
            )
        ),

        // 7. Rangpur Division (8 Districts)
        BdDistrict("dist_dinajpur", BdDivision.RANGPUR, "Dinajpur", "দিনাজপুর", 25.6279, 88.6332),
        BdDistrict("dist_gaibandha", BdDivision.RANGPUR, "Gaibandha", "গাইবান্ধা", 25.3288, 89.5280),
        BdDistrict("dist_kurigram", BdDivision.RANGPUR, "Kurigram", "কুড়িগ্রাম", 25.8054, 89.6362),
        BdDistrict("dist_lalmonirhat", BdDivision.RANGPUR, "Lalmonirhat", "লালমনিরহাট", 25.9923, 89.2847),
        BdDistrict("dist_nilphamari", BdDivision.RANGPUR, "Nilphamari", "নীলফামারী", 25.9318, 88.8561),
        BdDistrict("dist_panchagarh", BdDivision.RANGPUR, "Panchagarh", "পঞ্চগড়", 26.3411, 88.5542),
        BdDistrict("dist_rangpur", BdDivision.RANGPUR, "Rangpur", "রংপুর", 25.7439, 89.2752),
        BdDistrict("dist_thakurgaon", BdDivision.RANGPUR, "Thakurgaon", "ঠাকুরগাঁও", 26.0337, 88.4617),

        // 8. Mymensingh Division (4 Districts)
        BdDistrict("dist_jamalpur", BdDivision.MYMENSINGH, "Jamalpur", "জামালপুর", 24.9186, 89.9577),
        BdDistrict("dist_mymensingh", BdDivision.MYMENSINGH, "Mymensingh", "ময়মনসিংহ", 24.7471, 90.4203),
        BdDistrict("dist_netrokona", BdDivision.MYMENSINGH, "Netrokona", "নেত্রকোণা", 24.8709, 90.7279),
        BdDistrict("dist_sherpur", BdDivision.MYMENSINGH, "Sherpur", "শেরপুর", 25.0205, 90.0153)
    )

    val defaultDistrict: BdDistrict = districts.first { it.id == "dist_dhaka" }

    fun districtsForDivision(division: BdDivision): List<BdDistrict> =
        districts.filter { it.division == division }

    fun findDistrictById(id: String?): BdDistrict? =
        districts.firstOrNull { it.id == id }

    fun toCityLocation(
        district: BdDistrict,
        upazila: BdUpazila? = null,
        isGpsDetected: Boolean = false
    ): CityLocation {
        val lat = upazila?.latitude ?: district.latitude
        val lng = upazila?.longitude ?: district.longitude
        val displayEn = if (upazila != null) "${upazila.nameEn}, ${district.nameEn}" else district.nameEn
        val displayBn = if (upazila != null) "${upazila.nameBn}, ${district.nameBn}" else district.nameBn
        return CityLocation(
            id = upazila?.id ?: district.id,
            nameEn = displayEn,
            nameBn = displayBn,
            countryEn = "Bangladesh",
            countryBn = "বাংলাদেশ",
            latitude = lat,
            longitude = lng,
            timeZoneId = BANGLADESH_TIMEZONE_ID,
            isGpsDetected = isGpsDetected,
            divisionNameEn = district.division.nameEn,
            divisionNameBn = district.division.nameBn,
            upazilaNameEn = upazila?.nameEn ?: "",
            upazilaNameBn = upazila?.nameBn ?: "",
            isReferenceCoordinate = !isGpsDetected
        )
    }

    fun isInsideBangladeshBounds(latitude: Double, longitude: Double): Boolean =
        latitude in 20.5..26.75 && longitude in 88.0..92.75

    fun findNearestDistrict(latitude: Double, longitude: Double): BdDistrict? {
        if (!isInsideBangladeshBounds(latitude, longitude)) return null
        val nearest = districts.minByOrNull { d -> haversineKm(latitude, longitude, d.latitude, d.longitude) }
            ?: return null
        val distanceKm = haversineKm(latitude, longitude, nearest.latitude, nearest.longitude)
        return if (distanceKm <= 95.0) nearest else null
    }

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
