import java.util.Arrays;

public class Symbols {

  public static <T> T[] concatAll(T[] first, T[]... rest) {
    int totalLength = first.length;
    for (T[] array : rest) {
      totalLength += array.length;
    }
    T[] result = Arrays.copyOf(first, totalLength);
    int offset = first.length;
    for (T[] array : rest) {
      System.arraycopy(array, 0, result, offset, array.length);
      offset += array.length;
    }
    return result;
  }

  private static final String [] ETFSymbols  = {
    // from  http://finance.yahoo.com/etf/browser/tv?f=0&c=0&cs=1&ce=1068
    // in order of most traded on 8/31/10
 "SPY", "XLF", "EEM", "QQQQ", "IWM", "FAZ", "SDS", "FAS", "TZA", "VXX", "SSO",
 "EFA", "TNA", "EWJ", "XLI", "XLE", "EWZ", "SMH", "XLK", "EWT", "QID",
 "USO", "FXI", "EWH", "VWO", "TBT", "SKF", "SPXU", "UNG", "XRT", "XLP",
 "GLD", "GDX", "IYR", "TWM", "BGZ", "DIA", "QLD", "XLB", "XLU", "XLV",
 "TLT", "SLV", "XLY", "UCO", "DXD", "UWM", "SH", "EWS", "XME", "IAU",
 "MDY", "XOP", "XHB", "SRS", "UYG", "BGU", "IEV", "OIH", "EWW", "EWU",
 "IVV", "JNK", "EWA", "VEU", "RTH", "VNQ", "EWY", "ERX", "IWF", "DDM",
 "KBE", "ILF", "AMJ", "DIG", "EWC", "IWN", "EEV", "RSP", "IWO", "EWM",
 "KRE", "RSX", "UPRO", "IWD", "SCO", "VTI", "UUP", "DUG", "SMN", "EDC",
 "PFF", "IYF", "IWB", "UYM", "SHV", "URE", "IJR", "LQD", "EPI", "EDZ",
 // Remove comment to get all ETFs
 /*
 "KIE", "EWQ", "EPP", "IVE", "IEF", "FXP", "DRV", "IWS", "OEF", "VEA",
 "DRN", "HYG", "GDXJ", "SCZ", "DBC", "EWG", "IWR", "FXE", "PFM", "IWP",
 "TYH", "SHY", "ERY", "VTV", "RWM", "IJH", "DOG", "TMV", "DBA", "EWP",
 "ICF", "SDY", "TIP", "VIG", "AGG", "DGP", "IVW", "VGK", "IBB", "VUG",
 "EUO", "OIL", "PSQ", "PPH", "PGX", "DJP", "DZZ", "IYZ", "VB", "BIL",
 "PGF", "CSJ", "FXY", "BND", "FXA", "SQQQ", "PVI", "PCY", "DVY", "IYM",
 "EZA", "FXC", "IYT", "IWV", "TYP", "EWO", "VV", "XBI", "MOO", "IFGL",
 "TQQQ", "MLPI", "MLPI", "VO", "EWL", "AGQ", "IYW", "FCG", "CEW", "KOL",
 "DBO", 
 // "AMLP", 
 "VXZ", "BSV", "PBW", "YCS", "DTO", "IGE", "SSG", "SIVR",
 "MWJ", "SRTY", "EUFN", "IAT", "SOXL", "EPU", "SLX", "USD", "EEB", "RJI",
 "PHO", "GSG", "XXV", "GLL", "IJS", "ZSL", "BAB", "MZZ", "BWX", "IYG",
 "SHM", "IHI", "PDP", "EMB", "TUR", "CVY", "EZU", "DBB", "IJT", "EFV",
 "THD", "PXH", "IEO", "URTY", "BXUC", "SCHF", "BRF", "EPV", "INP", "RKH",
 "VBK", "SIL", "PIN", "ECH", "SCHB", "BKF", "SGOL", "RJA", "SDOW", "GXG",
 "IGW", "KLD", "IYE", "RWR", "PSR", "VBR", "MVV", "PIE", "IPK", "ITM",
 "REW", "VGT", "PZA", "TLH", "PXQ", "IYJ", "BLV", "IDX", "IWC", "RWX",
 "TAN", "ITB", "TBF", "ADRE", "VOE", "FXD", "RSW", "ACWI", "DAG", "PSP",
 "BZQ", "SCHE", "EWD", "AAXJ", "LSC", "IEZ", "BIV", "MWN", "GCC", "TMF",
 "VOT", "CWI", "TFI", "FRN", "VAW", "CIU", "DBV", "MBB", "SCHX", "EFG",
 "IJJ", "RWW", "PHB", "PSJ", "EWI", "UBT", "CHIQ", "HHH", "STPZ", "SOXS",
 "EMLC", "PST", "GWX", "UGL", "PID", "MUB", "IDV", "PEY", "IEI", "IJK",
 "DGS", "WPS", "SEF", "EWX", "FXF", "BZF", "VXF", "DTN", "FXB", "GMF",
 "DLN", "SCHA", "DPK", "USL", "KXI", "PBD", "VCR", "CWB", "VNM", "VT",
 "BIK", "VPL", "SDD", "PXJ", "HAO", "PCEF", "PBJ", "INDY", "PRF", "VCSH",
 "VYM", "ITR", "IHF", "VPU", "IXC", "GRV", "UDN", "IPE", "GAF", "KCE",
 "IGN", "DSC", "VGSH", "VFH", "JKL", "XES", "IAI", "DBE", "PIO", "FXN",
 "GXC", "FDL", "DEM", "IOO", "GUR", "ELD", "PIV", "DBP", "ROM", "GAZ",
 "JJC", "BXDD", "GML", "BXDC", "FDN", "CYB", "LBJ", "GRU", "EWK", "FTY",
 "LHB", "SCPB", "PALL", "FXL", "IDU", "VDE", "MINT", "MINT", "BXUB", "PPA",
 "JKI", "RWK", "CZM", "BRXX", "BHH", "VSS", "XSD", "ACWX", "PWV", "IYY",
 "JJG", "DXJ", "IGV", "EFZ", "TIPZ", "VHT", "UDOW", "IGM", "EWN", "RFG",
 "QTEC", "IWW", "RSU", "DSV", "MGK", "REZ", "FXZ", "XPH", "IXN", "EMV",
 "IYH", "FEZ", "PWB", "JKK", "FNI", "CUT", "DRW", "PBP", "BDD", "DWM",
 "VIS", "SBB", "IGF", "EUM", "WIP", "NFO", "MXI", "SKK", "PBE", "FBT",
 "LIT", "FDM", "SAA", "BNO", "PBS", "MLPL", "EIS", "CFT", "ITE", "HYD",
 "TAO", "IWX", "DHS", "DWX", "KRS", "GEX", "UVT", "SCHV", "LAG", "FXH",
 "FGD", "AGZ", "PGJ", "SMB", "ULE", "IWY", "DLS", "FAN", "SCC", "VOX",
 "FVD", "AFK", "CGW", "MGC", "RPV", "ISI", "ITA", "DNO", "PNQI", "ADZ",
 "FXO", "EDV", "JKJ", "OLO", "CNDA", "VDC", "PPLT", "IXP", "XPP", "RJZ",
 "RWO", "DGL", "UGA", "XLG", "IHE", "JKH", "PSI", "EPOL", "MGV", "PRFZ",
 "RETS", "SMDD", "IYC", "RJN", "TUZ", "SUB", "PXF", "TYD", "ALT", "AIA",
 "DES", "ADRD", "JXI", "PWY", "GSP", "BXDB", "PWC", "AOA", "REM", "UMDD",
 "FIVZ", "PXN", "RXL", "XLUS", "IYK", "EFU", "SZK", "EZM", "UKF", "SGG",
 "MLN", "JKE", "MUNI", "UCI", "SIJ", "ZROZ", "ZROZ", "SCHP", "RBL", "RWJ",
 "TMW", "FVI", "STPP", "GBF", "HAP", "VCIT", "PKN", "GCE", "PZI", "DBS",
 "RZV", "PWJ", "CSM", "FRI", "PXE", "IIH", "IAK", "IWZ", "IXJ", "ROB",
 "DOO", "PDN", "JJS", "SCHO", "DON", "ADRA", "COW", "GWL", "SEA", "SEA",
 "NLR", "TYO", "PZT", "JSC", "MYY", "RWL", "MBG", "MUAA", "MUAA", "DGZ",
 "UVG", "CORN", "ENY", "SJH", "AOR", "AOM", "BBH", "TLO", "ELG", "RXD",
 "ONEQ", "GSC", "IFEU", "JO", "PWND", "SCHC", "MLPN", "PLND", "BJK", "IXG",
 "DNH", "PEJ", "DSI", "FLAT", "JKD", "RYJ", "TDN", "PIZ", "KRU", "SCHG",
 "BWZ", "DZK", "TWOL", "PSK", "FXM", "PKB", "BOM", "CHIX", "PZD", "FYX",
 "SCIF", "BRAZ", "ICN", "BABS", "YAO", "GVI", "BDH", "FAA", "DOL", "DTD",
 "DFJ", "VGLT", "PLW", "RPG", "UKK", "FEX", "PWT", "UXI", "NIB", "NYC",
 "EXI", "RFV", "ADRU", "KWT", "QAI", "IPU", "CZI", "BICK", "FTC", "CHIM",
 "JYF", "XLVS", "GRID", "ITF", "PXI", "FIW", "AOK", "CRBQ", "SCHR", "YCL",
 "EES", "BSCF", "MTK", "PJB", "MLPG", "PYZ", "PTF", "DLBS", "UBR", "ICLN",
 "LVL", "REK", "PUW", "PXR", "UPV", "SWH", "SBND", "SBND", "JKG", "PUI",
 "EET", "PGM", "COPX", "DDG", "INY", "PAF", "RTR", "FNX", "SMMU", "BSCC",
 "UNL", "DTYS", "IRV", "SJF", "BAL", "WOOD", "IPF", "DND", "ISHG", "FTA",
 "CHIB", "CSLS", "CSLS", "BSCH", "QQEW", "RYU", "UGE", "ELV", "IPW", "CQQQ",
 "VMBS", "EMG", "CHXX", "EWV", "DSG", "FCGL", "DBU", "DYY", "FXU", "PSAU",
 "DIM", "PTH", "SKOR", "PWO", "PICB", "BRAQ", "IPD", "IPN", "GLJ", "RZG",
 "SDK", "PAGG", "BSCG", "PSL", "FXR", "ELR", "TDV", "PQSC", "SMK", "FCGS",
 "BSCE", "PWP", "FVL", "PJP", "FLM", "FFR", "IST", "IBND", "LTPZ", "RYT",
 "EMIF", "PMR", "DTYL", "DTH", "QCLN", "XLFS", "UCC", "JKF", "RYH", "TTH",
 "LWC", "FDV", "TDD", "EXB", "DEW", "PTJ", "PTM", "UPW", "NY", "CXA",
 "UVU", "CRO", "RXI", "DGT", "IPS", "CMF", "SZR", "XLYS", "PRB", "EMM",
 "IRY", "DBN", "PYH", "MNA", "RYE", "TZV", "PFA", "BIB", "QQXT", "CSD",
 "XRO", "ESR", "XLKS", "RCD", "EIDO", "EIDO", "OTP", "BSCB", "LATM", "IFNA",
 "PKW", "GXF", "IGOV", "PCA", "RETL", "DTUL", "EPS", "PQBW", "RGI", "DKA",
 "PNXQ", "TOK", "PWZ", "FXG", "SJL", "RTL", "FDD", "VCLT", "JJA", "GMM",
 "DEF", "PJM", "PJF", "DRR", "TDH", "UWC", "GSW", "SZO", "JJT", "PRN",
 "BSCD", "DENT", "FAB", "DLBL", "TZL", "CPI", "PQZ", "BRIS", "ICI", "VRD",
 "FAD", "DEE", "UKW", "PIQ", "PAO", "CHII", "PEZ", "UBG", "HEDJ", "SFK",
 "HGI", "JJP", "PLK", "RTM", "FCHI", "ROI", "UTH", "IAH", "SDP", "XRU",
 "GRN", "ONEF", "EMFN", "EMFN", "EZY", "BRAF", "OOK", "MUAC", "MUAC", "RWG",
 "FPX", "EEO", "PTE", "IRO", "JJM", "CHIE", "DTUS", "WMH", "SCJ", "BNZ",
 "PJG", "DPU", "NYF", "LTL", "XLPS", "XLPS", "CU", "MUAE", "MUAE", "TZD",
 "JFT", "RHS", "WMCR", "INDL", "GRES", "TWON", "CLY", "TENZ", "TENZ", "JJN",
 "WMW", "LBND", "LBND", "EKH", "GMTB", "UCD", "AADR", "CMD", "GRI", "FCV",
 "FZB", "KROO", "EVX", "BWV", "JJE", "NASI", "PFI", "EEG", "PIC", "RYF",
 "IFSM", "XLIS", "PKOL", "GII", "FEU", "EQL", "IWL", "INR", "PQY", "TZI",
 "VGIT", "SBM", "UHN", "JPX", "EZJ", "EMT", "EXT", "CNY", "FXS", "XLES",
 "BIS", "TLL", "PSTL", "MUAF", "MUAF", "IFAS", "UBM", "PMNA", "AXUT", "MUAB",
 "MUAB", "WFVK", "EIRL", "MDD", "PTO", "AGF", "QABA", "MES", "RWV", "DOD",
 "EEH", "AXEN", "GSD", "JCO", "TDX", "FUE", "NUCL", "TZO", "GULF", "PLTM",
 "MCRO", "PEF", "BSC", "UST", "FOC", "URR", "BOS", "TZG", "MUAD", "MUAD",
 "PZJ", "JJU", "DNL", "EFN", "AXDI", "CRBA", "CRBI", "CRBI", "FMV", "WCAT",
 "EUSA", "XGC", "OTR", "UBN", "EU", "TZE", "AYT", "TXF", "UMX", "FUD",
 "PMA", "AGA", "PJO", "FIO", "JPP", "PGD", "BDG", "AXIT", "JVS", "GSZ",
 "GSO", "GSR", "RPX", "GMMB", "WREI", "YXI", "UXJ", "DFE", "EEN", "CZA",
 "GBB", "ERO", "UBD", "ULQ", "USV", "UAG", "GWO", "DDP", "TWQ", "EFO",
 "LD", "BVT", "BRIL", "EGPT", "GVT", "TWOZ", "AXFN", "AXID", "AXMT", "AXTE",
 "AXSL", "AXHE", "RPQ", "RFF", "FKL", "DJCI", "DJCI", "EMMT", "FEFN", "XLBS",
 "WXSP", "SPGH", "SPGH", "MKH", "DEB", "KME", "DPC", "DPN", "DRF", "DBR",
 "DDI", "DBT", "DGG", "EEZ", "JYN", "RMM", "RMS", "RRY", "RRZ", "UBC",
 "JEM", "USY", "PTD", "REA", "REC", "RFL", "RFN", "RHM", "RHO", "RTG",
 "RTW", "TGR", "BVL", "INDZ", "PTRP", "PBTQ",
 */
  };

  private static final String [] SP500Symbols = {

    "A", "AA", "AAPL", "ABC", "ABT", 
    // "ACS", java.io.FileNotFoundException
    "ADBE", "ADI", "ADM", "ADP", "ADSK", "AEE", "AEP", "AES", "AET", "AFL", "AGN", "AIG", "AIV", "AIZ", "AKAM", "AKS", 
    // "ALL",  java.sql.SQLException: near "ALL": syntax error
    "ALTR", "AMAT", "AMD", "AMGN", "AMP", "AMT", "AMZN", "AN", "ANF", "APA", "APC", "APD", "APH", "APOL", "ARG", "ATI", "AVB", "AVP", "AVY", "AXP", "AYE", "AZO", "BA", "BAC", "BAX", "BBBY", "BBT", "BBY", "BBY", "BCR", 
    // "BDK", java.io.FileNotFoundException
    "BDX", "BEN", 
    // "BF-B", java.sql.SQLException: near "-": syntax error
    "BHI", "BIG", "BIIB", 
    // "BJS", java.io.FileNotFoundException
    "BK", "BLL", "BMC", "BMS", "BMY", 
    // "BNI", java.io.FileNotFoundException
    "BRCM", "BSX", "BTU", "BXP", "C", "CA", "CAG", "CAH", "CAM", "CAT", "CB", "CBG", "CBS", "CCE", "CCL", "CEG", "CELG", "CEPH", "CF", "CFN", "CHK", "CHRW", "CI", "CIEN", "CINF", "CL", "CLX", "CMA", "CMCSA", "CME", "CMI", "CMS", "CNP", "CNX", "COF", "COG", "COH", "COL", "COP", "COST", "CPB", "CPWR", "CRM", "CSC", "CSCO", "CSX", "CTAS", "CTL", "CTSH", "CTXS", "CVG", "CVH", "CVS", "CVX", "D", "DD", "DE", "DELL", "DF", "DFS", "DGX", "DHI", "DHR", "DIS", "DNB", "DNR", "DO", "DOV", "DOW", "DPS", "DRI", "DTE", "DTV", "DUK", "DV", "DVA", "DVN", "DYN", "EBAY", "ECL", "ED", "EFX", "EIX", "EK", "EL", "EMC", "EMN", "EMR", "EOG", "EP", "EQR", "EQT", "ERTS", "ESRX", "ESV", "ETFC", "ETN", "ETR", "EXC", "EXPD", "EXPE", "F", "FAST", "FCX", "FDO", "FDX", "FE", "FHN", "FII", "FIS", "FISV", "FITB", "FLIR", "FLR", "FLS", "FMC", "FO", 
    // "FPL", symbol changed to nee
    "FRX", "FSLR", "FTI", "FTR", "GAS", "GCI", "GD", "GE", "GENZ", "GILD", "GIS", "GLW", "GME", "GNW", "GOOG", "GPC", "GPS", "GR", "GS", "GT", "GWW", "HAL", "HAR", "HAS", "HBAN", "HCBK", "HCN", "HCP", "HD", "HES", "HIG", "HNZ", "HOG", "HON", "HOT", "HPQ", "HRB", "HRL", "HRS", "HSP", "HST", "HSY", "HUM", "IBM", "ICE", "IFF", "IGT", "INTC", "INTU", "IP", "IPG", "IRM", "ISRG", "ITT", "ITW", "IVZ", 
    // "JAVA", java.io.FileNotFoundException
    "JBL", "JCI", "JCP", "JDSU", "JEC", "JNJ", "JNPR", "JNS", "JPM", "JWN", "K", "KBH", "KEY", "KFT", "KG", "KIM", "KLAC", "KMB", "KO", "KR", "KSS", "L", "LEG", "LEN", "LH", "LIFE", "LLL", "LLTC", "LLY", "LM", "LMT", "LNC", "LO", "LOW", "LSI", "LTD", "LUK", "LUV", "LXK", "M", "MA", "MAR", "MAS", "MAT", "MBI", "MCD", "MCHP", "MCK", "MCO", "MDP", "MDT", "MEE", "MET", "MFE", "MHP", "MHS", "MI", "MIL", "MKC", "MMC", "MMM", "MO", "MOLX", "MON", "MOT", "MRK", "MRO", "MS", "MSFT", "MTB", "MU", "MUR", "MWV", "MWW", "MYL", "NBL", "NBR", "NDAQ", "NEM", "NI", "NKE", "NOC", "NOV", "NOVL", "NSC", "NSM", "NTAP", "NTRS", "NU", "NUE", "NVDA", "NVLS", "NWL", "NWSA", "NYT", "NYX", "ODP", "OI", "OMC", "ORCL", "ORLY", "OXY", "PAYX", "PBCT", 
    // "PBG", java.io.FileNotFoundException
    "PBI", "PCAR", "PCG", "PCL", "PCLN", "PCP", "PCS", "PDCO", "PEG", "PEP", "PFE", "PFG", "PG", "PGN", "PGR", "PH", "PHM", "PKI", "PLD", "PLL", "PM", "PNC", "PNW", "POM", "PPG", "PPL", "PRU", "PSA", "PTV", "PWR", "PX", "PXD", "Q", "QCOM", "QLGC", "R", "RAI", "RDC", "RF", "RHI", "RHT", "RL", "ROK", "RRC", "RRD", "RSG", "RSH", "RTN", "RX", "S", "SBUX", "SCG", "SCHW", "SE", "SEE", "SHLD", "SHW", "SIAL", "SII", "SJM", "SLB", "SLE", "SLM", "SNA", "SNDK", "SNI", "SO", "SPG", "SPLS", "SRCL", "SRE", "STI", "STJ", "STR", "STT", "STZ", "SUN", "SVU", "SWK", "SWN", "SWY", "SYK", "SYMC", "SYY", "T", "TAP", "TDC", "TE", "TEG", "TER", "TGT", "THC", "TIE", "TIF", "TJX", "TLAB", "TMK", "TMO", "TROW", "TRV", "TSN", "TSO", "TSS", "TWC", "TWX", "TXN", "TXT", "UNH", "UNM", "UNP", "UPS", "USB", "UTX", "VAR", "VFC", 
    // "VIA-B", java.sql.SQLException: near "-": syntax error
  "VLO", "VMC", "VNO", "VRSN", "VTR", "VZ", "WAG", "WAT", "WDC", "WEC", "WFC", "WFMI", "WFR", "WHR", "WIN", "WLP", "WM", "WMB", "WMT", "WPI", "WPO", "WU", "WY", "WYN", "WYNN", "X", "XEL", "XL", "XLNX", "XOM", "XRAY", "XRX", "XTO", "YHOO", "YUM", "ZION", "ZMH"};

  public static final String [] SYMBOLS = concatAll( ETFSymbols, SP500Symbols );

  public static String SymbolsAsHtml() {
    StringBuilder sb = new StringBuilder();
    // System.out.println("<h1>ETF Symbols</h1>\n");
    sb.append("<h1>ETF Symbols</h1>\n");
    for ( int i = 0 ; i < ETFSymbols.length; i++ ) {
      // System.out.println("<a href =\"http://finance.yahoo.com/q?" + ETFSymbols[i] + ">" + ETFSymbols[i] + "</a>" );
      sb.append("<a href =\"http://finance.yahoo.com/q?" + ETFSymbols[i] + "\">" + ETFSymbols[i] + "</a>\n" );
      
    }
    // System.out.println("<h1>SP500 Symbols</h1>\n");
    sb.append("<h1>SP500 Symbols</h1>\n");
    for ( int i = 0 ; i < SP500Symbols.length; i++ ) {
      // System.out.println("<a href =\"http://finance.yahoo.com/q?" + SP500Symbols[i] + ">" + SP500Symbols[i] + "</a>" );
      sb.append("<a href =\"http://finance.yahoo.com/q?" + SP500Symbols[i] + "\">" + SP500Symbols[i] + "</a>\n" );
    }
    return sb.toString();
  }

  public static void main(String [] args ) {
    System.out.println(SymbolsAsHtml());
  }
}
