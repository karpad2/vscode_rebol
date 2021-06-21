;
; stampe za materijalno knjizenje
;

stamp_fakt_def_01: context
[
  ; lokalis:
  rekf: none ; rekord fakture
  rek0: none ; dokument mat.knjig.
  rek1: none ; magacin
  rek2: none ; partner

  podaci_dom_fakture: func [ br_fak[string!] /local tar0 ]
  [
    ;
    ; rek0: rekord finans. fakt.
    mysql_cmd [ "SELECT br_fak,br_dok,datum_dok,sif_part,pdv FROM dok_fakt WHERE br_fak='" br_fak "' " ]
    rek0: get_rekord first db if not retval [ alert "Fali finans.faktura!" return none ]
    ;
    ; rek2: partner
    either rek0/sif_part = "0" [ rek2: none ]
    [
      mysql_cmd [ "SELECT sifra,name1,name2,name3,mesto,ulica,pib,matbr,drzava FROM " firma/data ".partners WHERE sifra='" rek0/sif_part "' " ]
      rek2: get_rekord first db if not retval [ alert "Partner nije u bazi!" return none ]
    ]
    ;
    ; tar0: prom_fakt
    mysql_cmd
    [
      "SELECT t1.sif_robe,t1.kol AS izlaz,t1.cena AS jed_cena,"
      "t1.popust AS rabat,t1.tarif,t1.naziv AS naz_robe,t1.jed_mere,'' AS naziv2 "
      "FROM prom_fakt AS t1 "
      "WHERE t1.br_fak='" rek0/br_fak "' AND (t1.tip<='9' OR t1.tip='A' OR t1.tip='M') ORDER BY t1.rbr1 ASC,t1.rbr2 ASC "
    ]
    tar0: get_rekordset copy db
    if empty? tar0 [ alert "Fale redovi fakture!" return none ]
    return tar0
  ]
  
  set 'podaci_dom_racuna: func [ br_fak[string!] /local br_dok tar0 ]
  [
    ;
    ; adat-beolvasasok:
    ; rekf: rekord fakture
    mysql_cmd 
    [ 
      "SELECT br_rac,br_dok,tip_fak,datum,rok,datum_dok,otprema,namena,opis,opis1,opis2,"
      "predracun,dat_avans,avans18,pdv18,predracun_1,dat_avans_1,avans18_1,pdv18_1,"
      "predracun_2,dat_avans_2,avans18_2,pdv18_2,na1 "
      "FROM faktura WHERE br_rac='" br_fak "' " 
    ]
    rekf: get_rekord first db if not retval [ alert rejoin [ "Faktura " br_fak " nije u bazi!" ] return none ]
    ;
    if rekf/tip_fak = "R"
    [
      return podaci_dom_fakture br_fak
    ]
    ;
    br_dok: copy rekf/br_dok
    ; rek0: dokument
    mysql_cmd [ "SELECT br_dok,datum_dok,sif_mag,sif_part,pdv FROM dokument WHERE br_dok='" br_dok "' " ]
    rek0: get_rekord first db if not retval [ alert rejoin [ "Dokument " br_dok " nije u bazi!" ] return none ]
    ;
    ; rek1: magacin
    rek1: select magacin_blk to-integer rek0/sif_mag
    if none? rek1 [ alert "Magacin nije u bazi!" return none ]
    ;
    ; rek2: partner
    either rek0/sif_part = "0" [ rek2: none ]
    [
      mysql_cmd [ "SELECT sifra,name1,name2,name3,mesto,ulica,pib,matbr,drzava FROM " firma/data ".partners WHERE sifra='" rek0/sif_part "' " ]
      rek2: get_rekord first db if not retval [ alert "Partner nije u bazi!" return none ]
    ]
    ;
    ; tar0: msort
    ;
    switch/default rek1/vrsta
    [
      "roba"
      [
        mysql_cmd 
        [ 
          "SELECT t1.*,IFNULL((SELECT t2.naziv2 FROM roba AS t2 WHERE t2.sif_robe=t1.sif_robe),'') AS naziv2 "
          "FROM msort_roba AS t1 "
          "WHERE t1.br_dok='" br_dok "' " 
        ]
      ]
      "materijal"
      [
        mysql_cmd 
        [ 
          "SELECT t1.*,IFNULL((SELECT t2.naziv2 FROM materijal AS t2 WHERE t2.sif_mat=t1.sif_robe),'') AS naziv2 "
          "FROM msort_materijal AS t1 "
          "WHERE t1.br_dok='" br_dok "' " 
        ]
      ]
      "alat"
      [
        mysql_cmd 
        [ 
          "SELECT t1.*,IFNULL((SELECT t2.naziv2 FROM alat AS t2 WHERE t2.sifra=t1.sif_robe),'') AS naziv2 "
          "FROM msort_alat AS t1 "
          "WHERE t1.br_dok='" br_dok "' " 
        ]
      ]
      "usluga"
      [
        mysql_cmd 
        [ 
          "SELECT '' AS sif_robe,'' AS naziv2,opis1 AS naz_robe,"
          "opis2 AS opis,jed_mere,kolic AS izlaz,jed_cena,tarif,rabat "
          "FROM usluga WHERE br_rac = '" br_dok "' " 
        ]
      ]
    ][ alert "Magacin nije odredjen!" return none ]
    tar0: get_rekordset copy db 
    if not retval [ alert "Promet nije u bazi!" return none ]
    ;
    return tar0
  ]
  ; pozivna broj generalas ugy ahogy a CAMP1-ben is tortenik
  pozivna_broj_generalas: func [ br_fak[string!] /local nn i k a0 a1 mar kb ]
  [
    nn: copy ""
    for i 1 (length? br_fak) 1
    [
      k: int_value (copy/part at br_fak i 1)
      if (nn <> "") and (k = 0) and ((copy/part at br_fak i 1) <> "0") [ break ]
      if (k >= 0) and (k <= 9) 
      [
        nn: rejoin [ nn k ]
      ]
    ]
    a0: (int_value nn) * 100 / 97
    a1: int_value a0
    mar: a0 - a1
    a1: round/to (mar * 97) 1
    kb: 98 - a1
    while [ (length? nn) < 4 ] 
    [
      nn: rejoin [ "0" nn ]
    ]
    nn: copy/part at nn ((length? nn) - 3) 4
    kb: to-string kb
    while [ (length? kb) < 2 ] 
    [
      kb: rejoin [ "0" kb ]
    ]
    kb: rejoin [ "97  " kb "-" nn ]
    return kb
  ]
  ; podaci partnera
  partner_adatok: func [ prek[object!] /local sz1 ]
  [
    sz1: copy prek/name1
    if prek/name2 <> ""  [ append sz1 join "^/" prek/name2  ]
    if prek/name3 <> ""  [ append sz1 join "^/" prek/name3  ]
    if prek/ulica <> ""  [ append sz1 join "^/" prek/ulica  ]
    if prek/mesto <> ""  [ append sz1 join "^/" prek/mesto  ]
    if prek/drzava <> "" [ append sz1 join "^/" prek/drzava ]
    if prek/pib <> ""    [ append sz1 join "^/PIB: " prek/pib ]
    if prek/matbr <> ""  [ append sz1 join "^/MBR: " prek/matbr ]
    return sz1
  ]
  
  set 'stamp_fakture: func [ br_fak[string!] /local tar0 old row0 row1 tp0 vrp i1 szov szov1 y1 y2 kerek kor_ime uk_jed_vr 
                             uk_vr uk_iznos uk_por kol cena vred vr dok_cim poz_br pnev datv uk_plac n1 uk_rabat rabat uk_tarif tarif 
                             kurs avans uk_avans na1 naziv avans_pdv uk_avans_pdv za_isp za_isp_pdv vr_slovima opis fejlec1 fejlec2 ]
  [
    kerek: 0.01
    tp0: copy br_fak
    tar0: podaci_dom_racuna br_fak
    if none? tar0 [ exit ]
    datv: date_value rekf/datum
    if none? datv [ alert "Prvo izaberi datum valute!" exit ]
    datv: datv + int_value rekf/rok
    tp0: replace tp0 "/" "-"
    mysql_cmd
    [
      "SELECT CONCAT(t2.prezime,' ',t2.ime) AS prezime_ime "
      "FROM " firma/data ".korisnik AS t1 "
      "LEFT JOIN " firma/data ".zaposleni AS t2 ON t2.matbr=t1.mbr "
      "WHERE t1.korisnik='" korisnik "' "
    ]
    row1: get_rekord first db
    either none? row1/prezime_ime [ kor_ime: "" ][ kor_ime: copy row1/prezime_ime ]
    ;
    load_pdf_font ""
    load_pdf_font "T1"
    load_pdf_font "TB"
    load_pdf_font "A1"
    load_pdf_font "AB"
    load_pdf_font "AIB"
    pdf_blk: create_pdf 598x842 ; A4-es
    pdf_rek/fname: join dir_lokal rejoin [ "dokum_" tp0 ".pdf" ]
    pdf_rek/fmode: true
    pdf_rek/poz1: as-pair 20 (pdf_rek/pagesz/2 - 100) ; bal felso szele
    pdf_rek/poz2: as-pair (pdf_rek/pagesz/1 - 40) 20  ; jobb also szele
    pdf_rek/poz: 0x0 ; a lap aljan kezdi hogy uj lap kovetkezzen
    new_pdf_image pdf_blk to-string firma_rek/stamp_logo1
    old: 0
    i1:  0
    vr:  0.0       ; PDV za poziciju
    cena:   0.0    ; cena proizvoda
    vred:   0.0    ; osnovica za poziciju
    tarif:  0.0    ; PDV %
    uk_vr:  0.0    ; ukupno bez PDV
    uk_por: 0.0    ; ukupno PDV
    uk_iznos: 0.0  ; iznos za poziciju
    uk_rabat: 0.0  ; rabat ukupno
    uk_jed_vr: 0.0 ; ukupno po jed ceni
    ;
    avans: 0.0     ; vrednost avans bez PDV
    uk_avans:  0.0 ; uk avans bez PDV
    avans_pdv: 0.0 ; vrednost PDV za avans
    uk_avans_pdv: 0.0 ;uk PDV za avans 
    ;
    za_isp:     0.0 ;vrednost za uplatu bez PDV
    za_isp_pdv: 0.0 ; vrednost PDV za uplatu
    uk_plac:    0.0 ;ukupno za placenje
    ;
    y1: 0
    y2: 0
    ;
    dok_cim:    copy "Raèun br."
    vr_slovima: copy ""
    opis: rejoin [ rekf/opis rekf/opis1 rekf/opis2 ]
    poz_br: pozivna_broj_generalas br_fak
    n1: length? tar0
    ;
    pdfcom [ fn "AB" 12x11 ]
    szov1: txt_szoveg_tordel (partner_adatok rek2) 220
    y1: length? parse/all szov1 "^/"
    y1: max (y1 * 14 + 2) 16 ; cella szelesseg
    ; fejlec alapadatok
    fejlec1: compose
    [
      fn "A1" 10 cg 1.0 lw 0.50 ct 0.0 cb 1.0 tv middle ta left
      at (pdf_rek/poz1 + 280x-30) ta right (rejoin [ "U Adi dana: " to-reg-date rekf/datum_dok ]) 280x12
      nl 280x12 tv bottom ta right (rejoin [ "Kupac: " rek2/sifra ]) 280x12
      nl 280x1 ln 280x0
      fn "AB" 12
      nl (as-pair 280 y1) ta center (szov1) (as-pair 280 y1)
      nl 280x1 ln 280x0
      ;
      nl 0x28 fn "AB" 16 tv middle ta center (rejoin [ dok_cim ": " br_fak ]) 560x18
      ;
      fn "A1" 10 tv middle ta left
      nl 0x16 ta left (rejoin [ "Poziv na broj: " poz_br ]) 560x12
      nl 0x12 ta left (rejoin [ "Datum prometa dobara i usluga: " to-reg-date rekf/datum_dok ]) 560x12
      nl 0x12 ta left (rejoin [ "Otpremnica br: " rekf/br_dok " od: " to-reg-date rekf/datum ]) 560x12
      nl 0x12 ta left (rejoin [ "Naèin otpreme : " rekf/otprema ]) 560x12
      nl 0x12 ta left (rejoin [ "Plaæanje u roku od " rekf/rok " dana /VA/ : " to-reg-date datv ]) 560x12
      nl 0x12 ta left (rejoin [ "Naèin plaæanja : " rekf/namena ]) 390x12
    ]
    ; tablazat fejlec
    fejlec2: compose
    [
      fn "A1" 10 cb 0.50 cg 0.8 tv middle
      nl 0x24 ta center "Rbr"             25x24
                        "Šifra"           55x24
                        "Naziv robe"     140x24
                        "Kol"             30x24
                        "Jed.^/mere"      30x24
                        "Jed. cena^/RSD"  55x24
                        "Rabat^/%"        30x24
                        "Osnovica^/RSD"   55x24
                        "PDV^/%"          25x24
                        "Iznos PDV^/RSD"  55x24
                        "Iznos^/RSD"      60x24
    ]
    pdfcom compose fejlec1
    pdfcom compose fejlec2
    foreach row0 tar0
    [
      y1: 90
      ; ha csak 3 sor maradt akkor ellenorzom hogy a szamla also resze rafer e majd a lapra 
      if (n1 - i1) < 4
      [
        y1: y1 + 170
        if opis <> "" [ y1: y1 + 35 ]
        if rekf/predracun   <> "" [ y1: y1 + 30 ]
        if rekf/predracun_1 <> "" [ y1: y1 + 14 ]
        if rekf/predracun_2 <> "" [ y1: y1 + 14 ]
      ]
      if (pdf_rek/poz/2 - y1) < pdf_rek/poz2/2      
      [
        pdfcom compose
        [
          nl 0x10 ta left "Nastavlja se..." 500x10 ta right (money_form (uk_vr + uk_por) 2) 60x10
        ]
        new_pdf_page pdf_blk ""
        old: old + 1 
        pdfcom compose fejlec1
        pdfcom compose
        [
          ta right (rejoin ["Strana: " old]) 170x14
        ]
        pdfcom compose fejlec2
      ]
      ;either row0/naziv2 <> "" [szov: txt_szoveg_tordel row0/naziv2 36] [szov: txt_szoveg_tordel row0/naz_robe 36]
      ; naziv2 megjelenitese ha ki van jelolve a checkbox
      either (rekf/na1 = "d") and (row0/naziv2 <> "")
      [ 
        naziv: rejoin [ row0/naz_robe " - " row0/naziv2 ] 
      ] 
      [ 
        naziv: copy row0/naz_robe 
      ]
      szov: txt_szoveg_tordel naziv 34
      y1: length? parse/all szov "^/"
      y1: max (y1 * 10) 10 ; cella szelesseg
      i1: i1 + 1
      kol:  dec_value row0/izlaz
      cena: dec_value row0/jed_cena
      vred: kol * cena
      uk_jed_vr: uk_jed_vr + (round/to vred kerek) 
      ; rabat:
      if (dec_value dec_value row0/rabat) <> 0.0
      [
        rabat: (vred * (dec_value row0/rabat) / 100)
        rabat: round/to rabat kerek
        uk_rabat: uk_rabat + rabat
        vred: vred - rabat
      ]
      ; porez:
      if rek0/pdv <> "N"
      [
        tarif: round/to (dec_value row0/tarif) kerek
        vr: vred * tarif / 100
        vr: round/to vr kerek
        uk_por: uk_por + vr
        uk_por: round/to uk_por kerek
      ]
      vred: round/to vred kerek
      uk_iznos: vred + vr
      uk_vr: uk_vr + vred
      uk_vr: round/to uk_vr kerek
      pdfcom compose
      [        
        nl (as-pair 0 y1) fn "A1" 8 cb 0.50 cg 1.0 tv middle        
        nl 0x0 lw 0.15 ln 560x0
        ta center (to-string i1)            (as-pair 25 y1)
        ta center (row0/sif_robe)           (as-pair 55 y1)
        ta left   (szov)                    (as-pair 140 y1)
        ta right  (trim dec_form kol 14 2)  (as-pair 30 y1)
        ta center (lowercase row0/jed_mere) (as-pair 30 y1)
        ta right  (money_form cena 2)       (as-pair 55 y1)
        ta center (money_form row0/rabat 2) (as-pair 30 y1)
        ta right  (money_form vred 2)       (as-pair 55 y1)
        ta center (money_form tarif 2)      (as-pair 25 y1)
        ta right  (money_form vr 2)         (as-pair 55 y1)
        ta right  (money_form uk_iznos 2)   (as-pair 60 y1)
      ]
    ]
    uk_plac: uk_vr
    if uk_rabat <> 0.0
    [
      pdfcom compose
      [
        nl 0x10 fn "AB" 8 cb 1.0 cg 1.0 tv middle
        ta left "Ukupno po jed. ceni:" 150x10 tb 330 
        ta right (money_form uk_jed_vr 2)  80x10
        nl 0x0 lw 0.15 ln 560x0
        nl 0x10 ta left "Rabat:" 130x10 tb 350 
        ta right (money_form uk_rabat 2)  80x10
        nl 0x0 lw 0.15 ln 560x0
      ]
    ]
    if rek0/pdv <> "N"
    [
      uk_por: round/to uk_por kerek
      uk_plac: uk_plac + uk_por
      pdfcom compose
      [
        nl 0x10 fn "AB" 8 cb 1.0 cg 1.0 tv middle
        ta left "Ukupno:" 60x10 tb 305
        ta right (money_form uk_vr 2)  55x10 tb 25
        ta right (money_form uk_por 2) 55x10 tb 1
        ta right (money_form (uk_vr + uk_por) 2) 60x10
        nl 0x0 lw 0.15 ln 560x0
      ]
    ]
    if rekf/predracun <> ""
    [
      avans:     dec_value rekf/avans18
      avans_pdv: dec_value rekf/pdv18
      uk_avans:  uk_avans + avans
      uk_avans_pdv: uk_avans_pdv + avans_pdv
      pdfcom compose
      [
        nl 0x10 fn "AB" 8 cb 1.0 cg 1.0 tv middle
        ta left (rejoin [ "Plaæeno avansnim raèunom: " rekf/predracun " od: " (to-reg-date rekf/dat_avans) ]) 260x10 tb 104 
        ta right (money_form avans 2)  60x10 tb 8 
        ta right (money_form avans_pdv 2) 60x10 tb 8 
        ta right (money_form (avans + avans_pdv) 2) 60x10
        nl 0x0 lw 0.15 ln 560x0
      ]
    ]
    if rekf/predracun_1 <> ""
    [
      avans:     dec_value rekf/avans18_1
      avans_pdv: dec_value rekf/pdv18_1
      uk_avans:  uk_avans + avans
      uk_avans_pdv: uk_avans_pdv + avans_pdv
      pdfcom compose
      [
        nl 0x10 fn "AB" 8 cb 1.0 cg 1.0 tv middle
        ta left (rejoin [ "Plaæeno avansnim raèunom: " rekf/predracun_1 " od: " (to-reg-date rekf/dat_avans_1) ]) 260x10 tb 104 
        ta right (money_form avans 2)  60x10 tb 8 
        ta right (money_form avans_pdv 2) 60x10 tb 8 
        ta right (money_form (avans + avans_pdv) 2) 60x10
        nl 0x0 lw 0.15 ln 560x0
      ]
    ]
    if rekf/predracun_2 <> ""
    [
      avans:     dec_value rekf/avans18_2
      avans_pdv: dec_value rekf/pdv18_2
      uk_avans:  uk_avans + avans
      uk_avans_pdv: uk_avans_pdv + avans_pdv
      pdfcom compose
      [
        nl 0x10 fn "AB" 8 cb 1.0 cg 1.0 tv middle
        ta left (rejoin [ "Plaæeno avansnim raèunom: " rekf/predracun_2 " od: " (to-reg-date rekf/dat_avans_2) ]) 260x10 tb 104 
        ta right (money_form avans 2)  60x10 tb 8 
        ta right (money_form avans_pdv 2) 60x10 tb 8 
        ta right (money_form (avans + avans_pdv) 2) 60x10
        nl 0x0 lw 0.15 ln 560x0
      ]
    ]
    if uk_avans <> 0.0
    [
      za_isp: uk_vr - uk_avans
      za_isp_pdv: uk_por - uk_avans_pdv
      uk_plac: za_isp + za_isp_pdv
      pdfcom compose
      [
        nl 0x10 fn "AB" 8 cb 1.0 cg 1.0 tv middle
        ta left "Iznos za uplatu:" 100x10 tb 264 
        ta right (money_form za_isp 2)  60x10 tb 8 
        ta right (money_form za_isp_pdv 2) 60x10 tb 8 
        ta right (money_form uk_plac 2) 60x10
        nl 0x0 lw 0.15 ln 560x0
      ]
    ]
    pdfcom compose
    [
      nl 0x12 fn "AB" 9 cb 1.0 cg 1.0 tv middle
      ta left "Ukupno za plaæanje:"   130x12 tb 350
      ta right (money_form uk_plac 2) 80x12      
      nl 0x0 lw 0.15 ln 560x0
    ]
    kurs: kurs_valute_nadan "EUR" rekf/datum
    uk_plac_eur: money_form (uk_plac / kurs) 2
    vr_slovima: rejoin [ "Slovima: " slovima uk_plac ]
    if uk_plac = 0.0
    [
      vr_slovima: copy "Slovima: nula dinara i 00/100"
    ]
    pdfcom compose
    [
      fn "A1" 10 tv middle
      nl 0x12 ta left (vr_slovima) 550x12
    ]
    if (uk_plac <> 0.0) or (opis <> "")
    [
      pdfcom compose
      [
        nl 0x20 ta left "Napomena:" 550x12
      ]
    ]
    szov: txt_szoveg_tordel opis 110
    y1: length? parse/all szov "^/"
    y1: max (y1 * 10) 10 ; cella szelesseg
    if opis <> ""
    [
      pdfcom compose
      [  
        nl (as-pair 0 y1) fn "A1" 10 tv middle ta left (szov) (as-pair 550 y1)
      ]
    ]
    if uk_plac > 0.0
    [
      pdfcom compose
      [  
        nl 0x12 ta left "Za neblagovremeno plaæanje zaraèunavamo zateznu kamatu naše poslovne banke." 550x12
        nl 0x12 ta left "Vrednost fakture iznosi " fn "AB" 10 tv bottom (uk_plac_eur) fn "A1" 10 tv bottom " eura." 550x12
        nl 0x12 ta left "Ako ne izvršite uplatu cele vrednosti fakture do dana isteka valute, plaæanje istog se vrši po srednjem " 550x12
        nl 0x12 ta left "kursu NBS na dan uplate, ukoliko se konstatuje odstupanje kursa izmedju dinara i eura veæe od 2%." 550x12
      ]
    ]
    pdfcom compose
    [
      at 20x110 cb 1.0 fn "A1" 12 cr 0.0 lw 0.8
      tv bottom ta left "Fakturisao:" 180x14 tb 220 "Generalni direktor:" 160x14
      nl 0x25 ln 110x0 tb 400 ln 110x0
      nl 0x16 tv bottom ta left (kor_ime) 180x14 tb 220 "Odgovorno lice" 160x14
    ]
    write_pdf_file pdf_blk
    if write_mode
    [
      mysql_cmd
      [
        "UPDATE faktura SET iznos='" uk_vr "',osn_por='" uk_por "',ukupno='" (uk_vr + uk_por) "' WHERE br_rac='" br_fak "' "
      ]
    ]
  ]
]

append cleanup
[
  stamp_fakt_def_01
  podaci_dom_racuna
  stamp_fakture
]
