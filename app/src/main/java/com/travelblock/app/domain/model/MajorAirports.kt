package com.travelblock.app.domain.model

/** The big airports travellers connect through. Boards and searches list these first, and long hauls stop only at them. */
object MajorAirports {
    private val codes: Set<String> = (
        // United States and territories
        "ATL ORD DFW DEN LAX JFK LAS MCO MIA SEA PHX EWR SFO IAH BOS FLL MSP LGA DTW PHL CLT SLC BWI DCA IAD SAN TPA PDX HNL ANC " +
            "STL MCI OKC TUL ABQ BNA AUS RDU SAT SMF SJC PIT CLE CMH CVG IND MSY MKE OAK ONT SNA BUR BOI ELP OMA RIC DAL HOU MDW " +
            "SDF MEM JAX RSW PBI BDL PVD BUF ALB TUS LIT DSM GEG OGG KOA SJU PSP MSN CHS RNO ABE " +
            // Canada, Mexico, Caribbean, Central and South America
            "YYZ YVR YUL YYC YEG YOW YWG YHZ MEX CUN GDL MTY TIJ PTY SJO SAL GUA HAV NAS PUJ KIN MBJ BGI " +
            "BOG MDE UIO GYE LIM SCL EZE AEP GRU GIG BSB CNF SSA REC FOR MVD ASU LPB CCS " +
            // Europe
            "LHR LGW MAN EDI DUB CDG ORY NCE LYS AMS BRU FRA MUC DUS HAM TXL BER CGN STR ZRH GVA VIE MAD BCN AGP PMI LIS OPO " +
            "FCO MXP VCE NAP ATH IST SAW SVO DME LED KEF CPH ARN OSL HEL WAW PRG BUD OTP SOF BEG KBP RIX VNO TLL " +
            // Middle East and Africa
            "DXB AUH DOH BAH KWI MCT RUH JED DMM AMM TLV BEY CAI JNB CPT DUR NBO ADD LOS ACC CMN ALG TUN DKR " +
            // Asia and Oceania
            "HND NRT KIX ITM NGO FUK CTS OKA ICN GMP PEK PKX PVG SHA CAN SZX CTU CKG XIY HKG MFM TPE BKK DMK HKT SIN KUL CGK DPS " +
            "MNL CEB SGN HAN DAD DEL BOM BLR MAA HYD CCU CMB DAC KTM ISB KHI LHE ULN TAS ALA " +
            "SYD MEL BNE PER ADL AKL WLG CHC NAN POM"
        ).split(' ').toSet()

    /**
     * Major airports that are the second airport of a city that has a bigger one (Love Field next to DFW, Hobby next to
     * IAH, Midway next to O'Hare...). They still count as major, but a connection or a trip prefers the main airport.
     */
    private val secondary: Set<String> = (
        "DAL HOU MDW LGA BUR ONT SNA OAK ITM ORY LGW SAW DMK SHA GMP AEP TXL"
        ).split(' ').toSet()

    fun isMajor(iata: String?): Boolean = iata != null && iata.uppercase() in codes

    fun isSecondary(iata: String?): Boolean = iata != null && iata.uppercase() in secondary
}

val Airport.isMajorHub: Boolean
    get() = MajorAirports.isMajor(iata)

/** A major airport that a bigger one in the same city should win over when both would do. */
val Airport.isSecondaryHub: Boolean
    get() = MajorAirports.isSecondary(iata)
