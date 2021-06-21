;
; Ino - Fakturisanje:
;
; globalis:
;  rek - rekord ino-fakture , rekord univerzalne fakture

ino_fakt_def_01: context
[
  ; lokalis:
  faktura_sif:  none ; sifra ino-faktura
  rek0:         none ; rekord otpremnice
  rek1:         none ; rekord globalne fakture
  rek2:         none ; rekord dokumenta prodaje
  prikaz_tr:    none ; prikaz dod.troska
  ablak_mp:     none ; seged-ablak
  filt1:        none ; ide gyujtom a lezarando fakturakat
  paym_dat:     none ; rok placanja datum
  mode:         none
  mode1:        none
  sif_part:  copy "" ; a partner kivalasztasahoz, az isvestajoknal
  naz_part:  copy "" ; a partner kivalasztasahoz, az isvestajoknal
  banka1:    copy "" ;
  banka2:    copy "" ;
  
  update_serija_prod: func [ srek[object!] /local row1 ]
  [
    mysql_cmd
    [
      "UPDATE prodaja_serija SET faza_ser='" srek/faza_ser "',br_fak='" srek/br_fak "' "
      "WHERE br_prod='" srek/br_prod "' AND serija='" srek/serija "' "
    ]
    mysql_cmd [ "SELECT MIN(faza_ser) AS faza_dok FROM prodaja_serija WHERE br_prod='" srek/br_prod "' " ]
    row1: get_rekord first db
    if retval
    [
      if row1/faza_dok <> srek/faza_dok
      [
        srek/faza_dok: copy row1/faza_dok
        mysql_cmd [ "UPDATE dok_prodaje SET faza_dok='" srek/faza_dok "' WHERE br_prod='" srek/br_prod "' " ]
      ]
    ]
  ]
  
  ios_adatok_olvas: func [ mode[object!] /local tar1 sz0 sz1 ]
  [
    tar1: make block! []
    sz0: copy "WHERE 1 "
    sz1: copy ""
    ;
    if mode/vrsta_datum = "" [ alert "Izaberi vrsta datuma!" return tar1 ]
    if mode/vrsta_datum = "datum fakt."
    [
      append sz0 rejoin [ "AND (t1.date    BETWEEN " apjel (mode/dat_od_ios) apjel " AND " apjel (mode/dat_do_ios) apjel ") "
                          "AND (t1.faza<>'G' OR t1.faza<>'Z') " ]
      append sz1 rejoin [ "AND t2.datum<=" apjel (mode/dat_do_ios) apjel " " ]
    ]
    if mode/vrsta_datum = "datum valuta."
    [
      append sz0 rejoin [ "AND (t1.dat_val BETWEEN " apjel (mode/dat_od_ios) apjel " AND " apjel (mode/dat_do_ios) apjel ") " ]
      append sz1 rejoin [ "AND t2.datum<=" apjel (mode/dat_do_ios) apjel " " ]
    ]
    ;if mode/vrsta_datum = "datum valuta." [ append sz0 rejoin [ "AND (t1.dat_val BETWEEN '" mode/dat_od_ios "' AND '" mode/dat_do_ios "') AND (t1.faza<>'G' OR t1.faza<>'Z') " ] ]
    if mode/vrsta_datum = "open_invoice"
    [
      append sz0 rejoin [ "AND t1.date>=" apjel "2019-01-01" apjel " AND t1.date<=" apjel (mode/dat_izv) apjel " " ]
      append sz1 rejoin [ "AND t2.datum<=" apjel (mode/dat_izv) apjel " " ]
    ]
    ;if mode/vrsta_datum = "open_invoice"  [ append sz0 rejoin [ "AND t1.date>='2019-01-01' " ] ]
    if mode/partner <> "" [ append sz0 rejoin [ "AND t1.user=" apjel (mode/partner) apjel " "  ] ]
    ;
    mysql_cmd compose
    [
      "SELECT CONCAT(IFNULL(t4.valuta," apjel apjel ")," apjel "|" apjel ",t3.name1," apjel "|" apjel ",t1.user," apjel "|" apjel ",t1.dat_val) AS kulcs,"
      "t1.user,t1.br_fak,t1.uk_izn,t1.date,t1.dat_val,t3.name1 AS partner,IFNULL(t4.valuta," apjel apjel ") AS valuta,"
      "IFNULL((SELECT SUM(t2.upl_eur) FROM upl_fakt AS t2 WHERE t2.br_fak=t1.br_fak " (sz1) " GROUP BY br_fak),0) AS uplata "
      "FROM " baze_pret ".faktura2 AS t1 "
      "LEFT JOIN " firma/data ".partners AS t3 ON t3.sifra=t1.user "
      "LEFT JOIN dok_fakt AS t4 ON t4.br_fak=t1.br_fak "
      (sz0) 
      "ORDER BY valuta ASC,partner ASC,t1.user ASC,t1.br_fak ASC"
    ]
    tar1: get_rekordset/index copy db
    ;
    mysql_cmd compose
    [
      "SELECT CONCAT(t4.valuta," apjel "|" apjel ",t3.name1," apjel "|" apjel ",t1.user," apjel "|" apjel ",t1.dat_val) AS kulcs,"
      "t1.user,t1.br_fak,t1.uk_izn,t1.date,t1.dat_val,t3.name1 AS partner,IFNULL(t4.valuta," apjel apjel ") AS valuta,"
      "IFNULL((SELECT SUM(t2.upl_eur) FROM upl_fakt AS t2 WHERE t2.br_fak=t1.br_fak " (sz1) " GROUP BY br_fak),0) AS uplata "
      "FROM faktura2 AS t1 "
      "LEFT JOIN " firma/data ".partners AS t3 ON t3.sifra=t1.user "
      "LEFT JOIN dok_fakt AS t4 ON t4.br_fak=t1.br_fak "
      (sz0)
      "ORDER BY valuta ASC,partner ASC,t1.user ASC,t1.br_fak ASC"
    ]
    append tar1 get_rekordset/index copy db    
    sort/skip tar1 2  ;-- sorba rakom
    return tar1
  ]
  
  ios_pdf: func [ mode[object!] /local tar0 row0 k0 uk_iznos uk_uplata uk_saldo ukpart_izn ukpart_upl ukpart_sld old fp1 dok_iso y1 sz0 sz1 sz2 ]
  [
    sz0: copy ""
    k0:         0    ; saldo
    uk_iznos:   0    ; ukupno iznos
    uk_uplata:  0    ; ukupno uplata
    uk_saldo:   0    ; ukupno saldo
    ukpart_izn: 0
    ukpart_upl: 0
    ukpart_sld: 0
    ;
    tar0: ios_adatok_olvas mode
    if (length? tar0) = 0 [ alert "Nema podataka!" exit ]
    ;
    load_pdf_font "A1"
    load_pdf_font "AB"
    load_pdf_font "AIB"
    pdf_blk: create_pdf 598x842 ; portrait A4-es
    pdf_rek/fname: join dir_lokal rejoin [ mode/dat_od_ios "-" mode/dat_do_ios "-ios.pdf" ]
    pdf_rek/fmode: true
    pdf_rek/poz1: as-pair 20 (pdf_rek/pagesz/2 - 50) ; bal felso szele
    pdf_rek/poz2: as-pair (pdf_rek/pagesz/1 - 50) 20 ; jobb also szele
    pdf_rek/poz: 0x0 ; a lap aljan kezdi hogy uj lap kovetkezzen
    old: 0 ; oldal szamlalo mindig nullazni itt
    fp1: flash "Obrada podataka..."
    dok_iso: copy "___.___"  ; formular szama ha kell
    either firma_rek/logo_izbor1 = "2"
    [
      new_pdf_image pdf_blk to-string firma_rek/stamp_logo2     ; alapertelmezett fejlec ez az ujabb
      fejlec:
      [
        fn "A1" 12 cg 1.0 lw 0.25 tv middle ct 0.0 cb 1.0
        nl 0x0 ta left im win 135x19 0x0 
        nl 0x20 fn "AB" 12
        ta center "IOS Izvoz" 560x24
        fn "A1" 11 ta right tb -50 (rejoin [ dok_iso "^/Strana: " old ]) 50x24
        nl 0x20 ta center (rejoin [ "Od: " mode/dat_od_ios " Do: " mode/dat_do_ios ]) 560x16
      ]
    ]
    [
      new_pdf_image pdf_blk to-string firma_rek/stamp_logo
      fejlec:
      [
        fn "A1" 12 cg 1.0 lw 0.25 tv middle ct 0.0 cb 1.0
        nl 0x40 ta left im win 56x40 15x0 fn "AB" 12
        ta center "IOS Izvoz" 470x24
        fn "A1" 11 ta right (rejoin [ dok_iso "^/Strana: " old ]) 80x24
        nl 0x20 ta center (rejoin [ "Od: " mode/dat_od_ios " Do: " mode/dat_do_ios ]) 470x16
      ]
    ]
    fejlec2:
    [
      nl 0x10
      nl 0x30 fn "AB" 9 cg 0.85 lw 0.25 tv middle ct 0.0 cb 0.5
      "Šifra^/Part." 30x30
      "Partner" 150x30
      "Broj^/raèuna" 50x30
      "Datum^/raèuna" 50x30
      "Datum^/plaæanja" 50x30
      "Val^/uta" 20x30
      "Iznos"  70x30
      "Uk.^/plaæeno" 70x30
      "Saldo" 70x30
    ]
    new_pdf_page pdf_blk ""
    old: old + 1
    pdfcom compose fejlec
    pdfcom compose fejlec2
    pdf_font_select "A1" 8
    sz1: copy tar0/2/valuta
    ;sz2: copy tar0/2/user
    foreach [ sif0 row0 ] tar0
    [
      k0: (dec_value row0/uk_izn) - (dec_value row0/uplata)
      k0: round/to k0 0.01
      if k0 > 0
      [
        if (sz2 <> row0/user) and (ukpart_sld > 0)
        [
          sz3: rejoin [ "UKUPNO part " sz2 ":" ]
          pdfcom compose
          [
            nl 0x12 fn "AB" 8 cg 1.0 lw 0.25 ct 0.0 cb 1.0 tv middle ta right
            (sz3) 350x12
            cb 0.5
            (money_form ukpart_izn  2) 70x12
            (money_form ukpart_upl  2) 70x12
            (money_form ukpart_sld  2) 70x12
          ]
          ukpart_izn: ukpart_upl: ukpart_sld: 0.0
        ]
        if sz1 <> row0/valuta
        [
          sz3: rejoin [ "UKUPNO " sz1 ":" ]
          pdfcom compose
          [
            nl 0x12 fn "AB" 8 cg 1.0 lw 0.25 ct 0.0 cb 1.0 tv middle ta right
            (sz3) 350x12
            cb 0.5
            (money_form uk_iznos  2) 70x12
            (money_form uk_uplata 2) 70x12
            (money_form uk_saldo  2) 70x12
          ]
          uk_iznos: uk_uplata: uk_saldo: 0
        ]
        
        uk_iznos:   (dec_value uk_iznos  ) + (dec_value row0/uk_izn)
        uk_uplata:  (dec_value uk_uplata ) + (dec_value row0/uplata)
        uk_saldo:   (dec_value uk_saldo  ) + (dec_value k0)
        ukpart_izn: (dec_value ukpart_izn) + (dec_value row0/uk_izn)
        ukpart_upl: (dec_value ukpart_upl) + (dec_value row0/uplata)
        ukpart_sld: (dec_value ukpart_sld) + (dec_value k0)
        if (pdf_rek/poz/2 - 20) < pdf_rek/poz2/2
        [
          new_pdf_page pdf_blk ""
          old: old + 1
          pdfcom compose fejlec
          pdfcom compose fejlec2
        ]
        y1: max 12 pdf_szoveg_tordel row0/partner 150
        pdfcom compose 
        [
          nl (as-pair 0 y1) fn "A1" 8 cg 1.0 lw 0.25 ct 0.0 cb 0.5 tv middle ta left
          (row0/user) (as-pair 30 y1) 
          (row0/partner) (as-pair 150 y1)
          (row0/br_fak) (as-pair 50 y1)
          (row0/date) (as-pair 50 y1)
          (row0/dat_val) (as-pair 50 y1)
          ta right
          (row0/valuta) (as-pair 20 y1)
          (money_form row0/uk_izn 2)  (as-pair 70 y1)
          (money_form row0/uplata 2) (as-pair 70 y1)
          (money_form k0 2) (as-pair 70 y1)
        ]
        sz1: copy row0/valuta
        sz2: copy row0/user
      ]
    ]
    sz3: rejoin [ "UKUPNO part " sz2 ":" ]
    pdfcom compose
    [
      nl 0x12 fn "AB" 8 cg 1.0 lw 0.25 ct 0.0 cb 1.0 tv middle ta right
      (sz3) 350x12
      cb 0.5
      (money_form ukpart_izn  2) 70x12
      (money_form ukpart_upl  2) 70x12
      (money_form ukpart_sld  2) 70x12
    ]
    sz3: rejoin [ "UKUPNO " sz1 ":" ]
    pdfcom compose
    [
      nl 0x12 fn "AB" 8 cg 1.0 lw 0.25 ct 0.0 cb 1.0 tv middle ta right
      (sz3) 350x12
      cb 0.5
      (money_form uk_iznos 2) 70x12
      (money_form uk_uplata 2) 70x12
      (money_form uk_saldo 2) 70x12
    ]
    unview/only fp1
    write_pdf_file pdf_blk
  ]
  
  xml_rekap_ir_detaljno: func [ mode[object!] /local tar0 row0 tar1 row1 k0 uk_iznosp uk_iznos ukizn_val val0 sz1 sz2 sz3 i0 n0 ]
  [
    uk_iznos:  0    ; ukupno iznos
    uk_iznosp: 0    ; ukupno iznos po partnerima
    uk_uplata: 0    ; ukupno uplata
    uk_saldo:  0    ; ukupno saldo
    ukizn_val: 0    ; uk.iznos po valute
    i0: 0
    n0: 0
    val0: copy ""   ; 
    sz1:  copy ""
    sz2:  copy ""
    sz3:  copy ""
    ;
    if sif_part <> ""
    [
      sz1: rejoin [ "AND t1.user=" apjel sif_part apjel " " ]
      sz2: copy ",t1.uk_izn"
    ]
    mysql_cmd compose
    [
      "SELECT t1.br_fak,t1.user,t1.uk_izn,t1.date,MID(t1.date,1,4) AS godina,"
      "(SELECT name1 FROM " firma/data ".partners AS t2 WHERE t2.sifra=t1.user) AS partner,"
      "(SELECT t4.otp_valuta FROM " baze_pret ".dokument AS t4 WHERE t4.br_dok=t1.sif_dok) AS valuta "
      "FROM " baze_pret ".faktura2 AS t1 "
      "WHERE (t1.date BETWEEN '" mode/dat_od_ios "' AND '" mode/dat_do_ios "') " sz1
      "ORDER BY valuta ASC,partner ASC,t1.br_fak ASC "
    ]
    tar0: get_rekordset copy db
    mysql_cmd compose
    [
      "SELECT t1.br_fak,t1.user,t1.uk_izn,t1.date,MID(t1.date,1,4) AS godina,"
      "(SELECT name1 FROM " firma/data ".partners AS t2 WHERE t2.sifra=t1.user) AS partner,"
      "(SELECT t4.otp_valuta FROM dokument AS t4 WHERE t4.br_dok=t1.sif_dok) AS valuta "
      "FROM faktura2 AS t1 "
      "WHERE (t1.date BETWEEN '" mode/dat_od_ios "' AND '" mode/dat_do_ios "') " sz1
      "ORDER BY valuta ASC,partner ASC,t1.br_fak ASC "
    ]
    append tar0 get_rekordset copy db
    ;
    xml_blk: create_excel_doc
    xml_rek/fname: rejoin [ dir_eximp "Rekapitulacija_izl_rac_" sistime/isodate "." firma_rek/xml_ext ]
    xml_rek/fmode: true
    xml_rek/auto_size: true

    excel_com compose
    [
      sheet "ios_izvoz"
      column 40 180 40 80 80 80 50 70
      format "f:0" align "h:Center" "v:Center" "w:1" ; fejlec
      interior "c:#E6E6E6" "p:Solid"
      ;
      row  "h:30" interior 0 align "h:Center" font "b:1"
      nl 0x10 ${1|,fn|} ${1|,cb 1.0|} ${1|,cg 1.0|} tv middle
      cell ("Šif.^/Part.")
      cell ("Partner")
      cell ("Valuta")
      cell ("Br.fakt.")
      cell ("Datum fakt.")
      cell ("Iznos")
      cell ("Godina")
    ]
    if sif_part <> ""
    [
      excel_com compose
      [
        cell ("Br.fakt.")
      ]
    ]
    n0: length? tar0
    foreach row0 tar0
    [
      i0: i0 + 1
      if val0 = "" [ val0: copy row0/valuta ]
      ;
      if val0 <> row0/valuta
      [
        sz1: rejoin [ "Ukupno po " (val0) ":" ]
        excel_com compose
        [
          row interior 0 font "b:1"
          format "f:@" align "h:Right"
          cell merge 4 (sz1)
          format "f:###,##0.00" align "h:Right"
          cell (ukizn_val)
        ]
        ukizn_val: 0
      ]
      uk_iznosp: round/to (dec_value row0/uk_izn) 0.01
      ukizn_val: ukizn_val + (dec_value row0/uk_izn)
      ukizn_val: round/to ukizn_val 0.01
      uk_iznos:  uk_iznos  + (dec_value row0/uk_izn)
      uk_iznos:  round/to uk_iznos 0.01
      excel_com compose
      [
        row interior 0 font "b:0"
        ;border "p:Top" "l:Continuous" "w:0.5" "p:Bottom" "l:Continuous" "w:0.5" "p:Left" "l:Continuous" "w:0.5" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
        format "f:@" align "h:Left"
        cell (row0/user)
        cell (row0/partner)
        format "f:@" align "h:Center"
        cell (row0/valuta)
        cell (row0/br_fak)
        cell (row0/date)
        format "f:###,##0.00" align "h:Right"
        cell (uk_iznosp)
        format "f:@" align "h:Center"
        cell (row0/godina)
       load_pdf_font "T1"
      ]
      if sif_part <> ""
      [
        excel_com compose
        [
          format "f:@" align "h:Left"
          cell (row0/br_fak)
        ]
      ]
      ;
      if i0 = n0
      [
        sz1: rejoin [ "Ukupno po " (row0/valuta) ":" ]
        excel_com compose
        [
          row interior 0 font "b:1"
          ;border "p:Top" "p:Bottom" "p:Left" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
          format "f:@" align "h:Right"
          cell merge 4 (sz1)
          format "f:###,##0.00" align "h:Right"
          cell (ukizn_val)
        ]
        ukizn_val: 0
      ]
      ;
      val0: copy row0/valuta
    ]
    ;-- meg nem hasznalom
    if sif_part = ""
    [
      excel_com compose
      [
        row interior 0 font "b:1"
        ;border "p:Top" "p:Bottom" "p:Left" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
        format "f:@" align "h:Right"
        cell merge 4 "Ukupno:"
        format "f:###,##0.00" align "h:Right"
        cell (uk_iznos)
      ]
    ]
    excel_com
    [
      autosize
      endsheet
    ]
    write_xml_doc xml_blk
  ]
  
  xml_rekap_ir: func [ mode[object!] /local tar0 row0 tar1 row1 k0 uk_iznosp uk_iznos ukizn_val val0 sz1 sz2 sz3 i0 n0 ]
  [
    uk_iznos:  0    ; ukupno iznos
    uk_iznosp: 0    ; ukupno iznos po partnerima
    uk_uplata: 0    ; ukupno uplata
    uk_saldo:  0    ; ukupno saldo
    ukizn_val: 0    ; uk.iznos po valute
    i0: 0
    n0: 0
    val0: copy ""   ; 
    sz1:  copy ""
    sz2:  copy ""
    sz3:  copy ""
    ;
    either sif_part = ""
    [ 
      sz2: copy ",SUM(t1.uk_izn) AS uk_izn"
      sz3: copy "GROUP BY t1.user "
    ]
    [
      sz1: rejoin [ "AND t1.user=" apjel sif_part apjel " " ]
      sz2: copy ",t1.uk_izn"
    ]

    mysql_cmd compose
    [
      "SELECT t1.br_fak,t1.user" sz2 ",t1.date,MID(t1.date,1,4) AS godina,"
      "(SELECT name1 FROM " firma/data ".partners AS t2 WHERE t2.sifra=t1.user) AS partner,"
      "(SELECT t4.otp_valuta FROM " baze_pret ".dokument AS t4 WHERE t4.br_dok=t1.sif_dok) AS valuta "
      "FROM " baze_pret ".faktura2 AS t1 "
      "WHERE (t1.date BETWEEN '" mode/dat_od_ios "' AND '" mode/dat_do_ios "') " sz1
      sz3
      "ORDER BY valuta ASC,partner ASC,t1.br_fak ASC "
    ]
    tar0: get_rekordset copy db
    mysql_cmd compose
    [
      "SELECT t1.br_fak,t1.user" sz2 ",t1.date,MID(t1.date,1,4) AS godina,"
      "(SELECT name1 FROM " firma/data ".partners AS t2 WHERE t2.sifra=t1.user) AS partner,"
      "(SELECT t4.otp_valuta FROM dokument AS t4 WHERE t4.br_dok=t1.sif_dok) AS valuta "
      "FROM faktura2 AS t1 "
      "WHERE (t1.date BETWEEN '" mode/dat_od_ios "' AND '" mode/dat_do_ios "') " sz1
      sz3
      "ORDER BY valuta ASC,partner ASC,t1.br_fak ASC "
    ]
    append tar0 get_rekordset copy db
    ;
    xml_blk: create_excel_doc
    xml_rek/fname: rejoin [ dir_eximp "Rekapitulacija_izl_rac_" sistime/isodate "." firma_rek/xml_ext ]
    xml_rek/fmode: true
    xml_rek/auto_size: true

    excel_com compose
    [
      sheet "ios_izvoz"
      column 40 180 40 80 50 70
      format "f:0" align "h:Center" "v:Center" "w:1" ; fejlec
      interior "c:#E6E6E6" "p:Solid"
      ;
      row  "h:30" interior 0 align "h:Center" font "b:1"
      cell ("Šif.^/Part.")
      cell ("Partner")
      cell ("Valuta")
      cell ("Iznos")
      cell ("Godina")
    ]
    if sif_part <> ""
    [
      excel_com compose
      [
        cell ("Br.fakt.")
      ]
    ]
    n0: length? tar0
    foreach row0 tar0
    [
      i0: i0 + 1
      if val0 = "" [ val0: copy row0/valuta ]
      ;
      if val0 <> row0/valuta
      [
        sz1: rejoin [ "Ukupno po " (val0) ":" ]
        excel_com compose
        [
          row interior 0 font "b:1"
          format "f:@" align "h:Right"
          cell merge 2 (sz1)
          format "f:###,##0.00" align "h:Right"
          cell (ukizn_val)
        ]
        ukizn_val: 0
      ]
      uk_iznosp: round/to (dec_value row0/uk_izn) 0.01
      ukizn_val: ukizn_val + (dec_value row0/uk_izn)
      ukizn_val: round/to ukizn_val 0.01
      uk_iznos:  uk_iznos  + (dec_value row0/uk_izn)
      uk_iznos:  round/to uk_iznos 0.01
      excel_com compose
      [
        row interior 0 font "b:0"
        ;border "p:Top" "l:Continuous" "w:0.5" "p:Bottom" "l:Continuous" "w:0.5" "p:Left" "l:Continuous" "w:0.5" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
        format "f:@" align "h:Left"
        cell (row0/user)
        cell (row0/partner)
        format "f:@" align "h:Center"
        cell (row0/valuta)
        format "f:###,##0.00" align "h:Right"
        cell (uk_iznosp)
        format "f:@" align "h:Center"
        cell (row0/godina)
      ]
      if sif_part <> ""
      [
        excel_com compose
        [
          format "f:@" align "h:Left"
          cell (row0/br_fak)
        ]
      ]
      ;
      if i0 = n0
      [
        sz1: rejoin [ "Ukupno po " (row0/valuta) ":" ]
        excel_com compose
        [
          row interior 0 font "b:1"
          ;border "p:Top" "p:Bottom" "p:Left" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
          format "f:@" align "h:Right"
          cell merge 2 (sz1)
          format "f:###,##0.00" align "h:Right"
          cell (ukizn_val)
        ]
        ukizn_val: 0
      ]
      ;
      val0: copy row0/valuta
    ]
    ;-- meg nem hasznalom
    if sif_part = ""
    [
      excel_com compose
      [
        row interior 0 font "b:1"
        ;border "p:Top" "p:Bottom" "p:Left" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
        format "f:@" align "h:Right"
        cell merge 2 "Ukupno:"
        format "f:###,##0.00" align "h:Right"
        cell (uk_iznos)
      ]
    ]
    excel_com
    [
      autosize
      endsheet
    ]
    write_xml_doc xml_blk
  ]
  
  xml_naimenov_jci: func [ mode[object!] /local tar0 row0 mag vred popust mat_gr fp1 i1 tar_len ]
  [ 
    prodgrupe:   make block! [] 
    logfh:  make string! 1000000
    mat_gr: ""
    vred:   0.00
    popust: 0.00
    i1:     0
    mysql_cmd compose
    [
      "SELECT t1.vrsta FROM magacin AS t1 WHERE t1.sif_mag = '" mode/sif_mag "' "
    ]
    mag: get_rekord first db
    ;
    if mag/vrsta = "roba"
    [
      mysql_cmd compose
      [
        "SELECT t1.br_dok,t1.red_br,t1.sif_robe,t1.izlaz,t1.jed_mere,t1.rabat,t1.eur_cena,"
        "t1.sif_robe,t1.naz_robe,t2.tarif_car,t2.tezina,t2.mat_grupa,t3.otp_valuta "
        "FROM msort_roba AS t1 "
        "LEFT JOIN roba AS t2 ON t2.sif_robe=t1.sif_robe "
        "LEFT JOIN dokument AS t3 ON t3.br_dok=t1.br_dok "
        "WHERE t1.br_dok = '" mode/sif_dok "' "
      ]
      tar0: get_rekordset copy db
    ]
    if mag/vrsta = "materijal"
    [
      mysql_cmd compose
      [
        "SELECT t1.br_dok,t1.red_br,t1.sif_robe,t1.izlaz,t1.jed_mere,t1.rabat,t1.eur_cena,"
        "t1.sif_robe,t1.naz_robe,t2.tarif_car,t2.tezina,t2.mat_grupa,t3.otp_valuta "
        "FROM msort_materijal AS t1 "
        "LEFT JOIN materijal AS t2 ON t2.sif_mat=t1.sif_robe "
        "LEFT JOIN dokument AS t3 ON t3.br_dok=t1.br_dok "
        "WHERE t1.br_dok = '" mode/sif_dok "' "
      ]
      tar0: get_rekordset copy db
    ]
    if mag/vrsta = "alat"
    [
      mysql_cmd compose
      [
        "SELECT t1.br_dok,t1.red_br,t1.sif_robe,t1.izlaz,t1.jed_mere,t1.rabat,t1.eur_cena,"
        "t1.sif_robe,t1.naz_robe,t2.tarif_car,'' AS tezina,'' AS mat_grupa,t3.otp_valuta "
        "FROM msort_alat AS t1 "
        "LEFT JOIN alat AS t2 ON t2.sifra=t1.sif_robe "
        "LEFT JOIN dokument AS t3 ON t3.br_dok=t1.br_dok "
        "WHERE t1.br_dok = '" mode/sif_dok "' "
      ]
      tar0: get_rekordset copy db
    ]
    ;
    xml_blk: create_excel_doc
    xml_rek/fname: rejoin [ dir_eximp "naimenov_jci_" sistime/isodate "." firma_rek/xml_ext ]
    xml_rek/fmode: true
    xml_rek/auto_size: true
    fp1: flash "Obrada podataka..."
    ;
    excel_com compose
    [
      sheet "naimenov_jci"
      column 80 70 30 170 250 50 30 40 70 50 35
      format "f:0" align "h:Center" "v:Center" "w:1"
      interior "c:#E6E6E6" "p:Solid"
      row  "h:30" interior 0 align "h:Center" font "b:1"
      cell ("Br. prod.")
      cell ("Br. fakt.")
      cell ("Rbr")
      cell ("Trgovaèki naziv")
      cell ("Kategorija")
      cell ("Jed. mere")
      cell ("Kol.")
      cell ("Težina")
      cell ("Tarifa carine")
      cell ("Vrednost")
      cell ("Valuta")
    ]
    ;
    logfile_text rejoin
    [
      "  Pogrešno upisan tarifa carine: ^/"
      str_fill "-" 85 "^/"
      " " (str_form "Šifra robe"  13) (str_form "Naziv robe"  45) 
          (str_form "Zemlja por." 13) (str_form "Tarifa car." 25) "^/"
    ]
   
    foreach row0 tar0
    [ 
      vred: (dec_value row0/izlaz) * (dec_value row0/eur_cena)
      popust: (vred / 100) * (dec_value row0/rabat)
      vred: vred - popust  
      tar_len: length? row0/tarif_car 
      ;
      mat_gr: skip (blk_find g_prodgrupe/1 g_prodgrupe/2 row0/mat_grupa) 3
      if (tar_len <> 10) and (tar_len <> 0) and (mag/vrsta = "roba") 
      [
        logfile_text rejoin
        [
          " " (str_form row0/sif_robe   13) (str_form row0/naz_robe  45)   
              (str_form row0/zemlja_por 13) (str_form row0/tarif_car 25) "^/"
        ]    
        i1: i1 + 1 
      ]
      
      excel_com compose
      [
        row interior 0 font "b:0"
        format "f:@" align "h:Left"
        cell (mode/veza_dok)
        format "f:@" align "h:Center"
        cell (mode/br_fak)
        cell (row0/red_br)
        format "f:@" align "h:Left"
        cell (row0/naz_robe)
        cell (mat_gr)
        format "f:@" align "h:Center"
        cell (row0/jed_mere)
        format "f:0" align "h:Right"
        cell (to-integer row0/izlaz)
        cell (dec_form row0/tezina 0 2)
        format "f:0" align "h:Right"
        cell (row0/tarif_car)
        format "f:###,##0.00"
        cell (to-decimal (dec_form vred 0 2))
        format "f:@" align "h:Center"
        cell (row0/otp_valuta)
      ]
    ]
    excel_com
    [
      autosize
      endsheet
    ]    
    unview/only fp1
    either i1 <> 0
    [
      nyomtatas_txt reduce [ logfh ] "pogres_upis_tarif_car.txt"
    ]
    [
      write_xml_doc xml_blk
    ]
  ]
  
  xml_ios: func [ mode[object!] /local tar0 sif0 row0 k0 fp1 ]
  [
    k0:  0    ; saldo
    ;
    xml_blk: create_excel_doc
    xml_rek/fname: rejoin [ dir_eximp "IOS_na_dan_" sistime/isodate "." firma_rek/xml_ext ]
    xml_rek/fmode: true
    xml_rek/auto_size: true
    fp1: flash "Obrada podataka..."
    excel_com compose
    [
      sheet "ios_izvoz"
      ;column 100 60 300 250 60 100 150 150 150 100 60 70 150
      format "f:0" align "h:Center" "v:Center" "w:1" ; fejlec
      interior "c:#E6E6E6" "p:Solid"
      
      row  "h:30" interior 0 align "h:Center" font "b:1"
      cell ("Šifra^/Partnera")
      cell ("Partner")
      cell ("Br.raèuna")
      cell ("Datum raèuna")
      cell ("Datum plaæ.")
      cell ("Valuta")
      cell ("Iznos")
      cell ("Uk.plaæeno")
      cell ("Saldo")
    ]
    ;
    tar0: ios_adatok_olvas mode
    if (length? tar0) = 0 [ alert "Nema podataka!" exit ]
    foreach [ sif0 row0 ] tar0
    [
      k0: (dec_value row0/uk_izn) - (dec_value row0/uplata)
      k0: round/to k0 0.01
      ;
      if k0 > 0
      [      
        excel_com compose
        [
          row interior 0 font "b:0"
          format "f:@" align "h:Left"
          cell (row0/user)
          cell (row0/partner)
          format "f:@" align "h:Center"
          cell (row0/br_fak)
          format "f:@" align "h:Left"
          cell (row0/date)
          cell (row0/dat_val)
          format "f:@" align "h:Center"
          cell (row0/valuta)
          format "f:###,##0.00" align "h:Right"
          cell (dec_value row0/uk_izn)
          cell (dec_value row0/uplata)
          cell (dec_value k0)
        ]
      ]
    ]
    excel_com
    [
      autosize
      endsheet
    ]
    unview/only fp1
    write_xml_doc xml_blk
  ]
  
  ablak_cezar: func [/local t1_sif novi_sif bgod datum_od datum_do dat_od_ios dat_do_ios blk _fak saldo_zatv iso_dat ]
  [
    blk_fak:    none
    datum_od:   sistime/isodate
    datum_do:   sistime/isodate
    ;
    tabla_blk: compose
    [
      size ablak_c/size
      backtile polished color_f4
      at 10x10 btn "Sz:M" 40x24 color_l2
      [
        set_window_size 'm
        ablak_arajz
        ablak_brajz
        ablak_cezar
      ]
      at 100x10 text right "Prikaz dod.tr:" 120x24
      at 220x10 check prikaz_tr 24x24
      [
        prikaz_tr: face/data
      ]
      at 10x50 text right "Nova šifra:" 100x24
      at 110x50 t1_sif: field 120x24
      at 250x50 button "Upis" 80x24 color_m2
      [
        novi_sif: copy t1_sif/text
        if novi_sif = ""
        [
          if none? row1: oblik_dokum_rekord "FAK" "R" [ alert "Tip fakture nije dobar!" exit ]
          sz1: dokumentum_szam_keszito row1/dok_form2 baze_sif "faktura2" "br_fak"
          ;
          if string? sz1 [ novi_sif: copy sz1 ]
        ]
        if (length? novi_sif) < 4 [ alert "Broj dokumenta nije odreðen!" exit ]
        mysql_cmd [ "SELECT br_fak FROM faktura2 WHERE br_fak = '" novi_sif "'" ]
        if (not empty? first db)
        [
          alert rejoin [ ~"Faktura:" " '" novi_sif "' " ~"veæ postoji!" ] exit
        ]
        ; beirni az uj rekordot:
        faktura_sif: copy novi_sif
        mysql_cmd
        [
          "INSERT INTO faktura2 SET br_fak='" faktura_sif "',otp_valuta='EUR',otp_kurs='" firma_rek/kurs_euro "',"
          "best_nr='da',best_bk='ne',po_nar='da',zaglav='da',opis='',kor_insert='" korisnik "',dat_insert=CURDATE() "
        ]
        ablak_brajz
        mysql_cmd 
        [
          "SELECT t1.*,"
          "(SELECT t2.rok_pl FROM " firma/data ".partners AS t2 WHERE t2.sifra=t1.user) AS rok_pl,"
          "(SELECT COUNT(*) FROM faktura2 AS t3 WHERE t3.br_fak LIKE CONCAT(t1.br_fak,'%')) AS col_fakt "
          "FROM faktura2 AS t1 "
          "WHERE t1.br_fak='" faktura_sif "' " 
        ]
        rek: get_rekord first db
        either retval [ mod_faktura ][ alert "faktura nije u bazi!" faktura_sif: copy "" ]
      ]
      at 10x100 text right "Obrada svih faktura:" 220x24
      at 250x100 button "Obrada" 80x24 color_b2
      [
        obrada_ino_racuna
        ablak_brajz
      ]
    ]
    if find [ "prg" "adm" "sis" ] dozvola
    [
      append tabla_blk compose
      [
        at 10x150 text right "Prenos dodatnih troškova:" 220x24
        at 250x150 button "Prenos" 80x24 color_b2
        [
          bgod: request-text/title/default "Godina:" (copy baze_sif)
          if not string? bgod [ exit ]
          prenos_dod_troskova bgod
          ablak_brajz
        ]
        at 350x150 button "Gotovo sve" 120x24 color_m2
        [
          if not write_mode [ exit ]
          izbor_fraze "Upisati fazu G za:" [ "faza<'G'" "faza>'G'" ] none
          if not string? retval [ exit ]
          mysql_cmd compose [ "UPDATE faktura2 SET faza='G' WHERE " (retval) " " ]
          ablak_brajz
        ]
      ]
    ]
    append tabla_blk compose
    [
      at  5x200 box color_g4 550x330 with [ edge: [color: coal size: 1x1] ]
      at 10x205 text bold "Izveštaji:" 220x24
      at 10x240 text "Izvod Otvorenih Stavki:" 220x24
      ; itt volt az ios xml gomb
      at 250x240 btn "IOS <PDF>" 100x24 color_l2
      [
        ios_pdf mode
      ]
      at 360x240 btn "IOS <XML>" 100x24 color_l6
      [
        xml_ios mode
      ]
      at  10x280 text "Rekapitulacija izlazni raèuni:" 220x24
      at 250x270 btn "Rekap.IR^/<XML>" 100x38 color_l6
      [
        xml_rekap_ir mode
      ]
      at 360x270 btn "Rekap.IR-detalj.^/<XML>" 100x38 color_l6
      [
        xml_rekap_ir_detaljno mode
      ]
      at  10x330 text right "Partner:" 80x24
      at 110x330 arrow right 24x24 color_l2
      [
        sif_part: copy ""
        knjig_partner_izbor sif_part none "W0"
        if object? retval
        [
          sif_part:     copy retval/sifra
          naz_part:     copy retval/name1
          mode/partner: copy retval/sifra
          ablak_cezar
        ]
      ]
      at 140x330 info center (sif_part) 70x24 color_g2
      at 220x330 text naz_part 400x24
      at  10x370 text right "Vrsta datum:" 100x24
      at 110x370 drop-down data [ "datum fakt." "datum valuta." ] 150x24
      with [ rows: 3 text: mode/vrsta_datum ]
      [
        if string? face/data
        [
          mode/vrsta_datum: face/text
        ]
      ]
      at  10x410 text right "Datum od:" 100x24
      at 110x410 w_date mode/dat_od_ios 120x24
      at 230x410 text right "Datum do:" 100x24
      at 330x410 w_date mode/dat_do_ios 120x24
    ]
    if find [ "prg" "sis" ] dozvola
    [
      append tabla_blk compose
      [
        at 10x500 text right bold "Zatv.fakture:" 120x24
        at 10x550 text right "Datum od:" 100x24
        at 110x550 w_date datum_od 120x24
        at 230x550 text right "Datum do:" 100x24
        at 330x550 w_date datum_do 120x24
        at 110x600 button "Zatv.fakt." 100x24 color_b2
        [
          mysql_cmd compose
          [
            "SELECT t1.br_fak,t1.date,t1.uk_izn,t1.user,"
            "IFNULL((SELECT SUM(upl_eur) FROM upl_fakt AS t2 WHERE t2.br_fak=t1.br_fak),0) AS uk_upl,t1.faza "
            "FROM faktura2 AS t1 "
            "WHERE (t1.date BETWEEN '" (datum_od) "' AND '" (datum_do) "') AND t1.faza <> 'Z'"
          ]
          tar0: get_rekordset copy db
          blk_fak: make block![]
          foreach row0 tar0
          [
            saldo_zatv: (dec_value row0/uk_izn) - (dec_value row0/uk_upl)
            if saldo_zatv > 0
            [
              append blk_fak row0/br_fak
              mysql_cmd
              [
                "INSERT IGNORE INTO upl_fakt "
                "SET br_fak = '" row0/br_fak "',mbr='1',rbr='0',datum='" row0/date "',tip='I',sif_part='" row0/user "',"
                "upl_eur='" dec_value row0/uk_izn "',kurs='" firma_rek/kurs_euro "',vrsta='R',godina='2017'"
              ]
            ]
            if (saldo_zatv = 0) and (row0/faza <> "G") [ append blk_fak row0/br_fak ]
          ]
          filt1: mylist_form blk_fak
          mysql_cmd compose ; a faza-t Z-re allitom azoknal a fakturaknal ahol a saldo is 0
          [
            "UPDATE faktura2 "
            "SET faza = 'G' "
            "WHERE br_fak IN (" (filt1) ")"
          ]
          alert "Gotovo!!!"
        ]
        at  10x650 text bold "Pretvoriti payment datum u ISO format:" 350x24
        at 360x650 button "ISO format" 100x24 color_b2
        [
          iso_dat: none
          mysql_cmd compose
          [
            "SELECT t1.br_fak,t1.date,t1.payment FROM faktura2 AS t1 "
          ]
          tar0: get_rekordset copy db
          blk_fak: make block![]
          foreach row0 tar0
          [
            either (row0/payment = "Avansno") or (row0/payment = "Avans") or (empty? row0/payment) or (none? row0/payment)
            [ 
              if row0/date <> "" [ iso_dat: copy row0/date ]
            ]
            [
              blk_fak: parse/all row0/payment "."
              if (length? blk_fak) = 3 [ iso_dat: rejoin [ blk_fak/3 "-" blk_fak/2 "-" blk_fak/1 ] ]               
            ]
            mysql_cmd
            [
              "UPDATE faktura2 "
              "SET dat_val='" iso_dat "' "
              "WHERE br_fak='" row0/br_fak "'"
            ]
          ]
          alert "Gotovo!!!"
        ]
      ]
    ]
    ablak_c/pane: layout/offset tabla_blk 0x0
    show ablak_c
  ]
  
  rac_dom_iznos: func [ /local kurs vr1 ]
  [
    if rek/dat_istupa < "2000" [ return true ]
    if rek/dat_istupa > sistime/isodate [ alert "Datum istupa je veæi od današnji datum!" return false]
    if rek0/otp_valuta = "" [ alert "Nije definisana valuta!" return false ]
    ; srednji_kurs kereso: kurs_valute_nadan
    ; kurs_valute_nadan/euro - akkor ha nincs beirva valuta
    ; rek0/otp_valuta - valuta 
    ; rek/dat_istupa - kurs datuma
    kurs: kurs_valute_nadan rek0/otp_valuta rek/dat_istupa
    if kurs < 1 [ alert "Kurs nije definisan!" return false ]
    vr1: round/to (dec_value rek/uk_izn) 0.01
    vr1: round/to (vr1 * kurs) 0.01
    mysql_cmd
    [
      "UPDATE faktura2 SET uk_din='" vr1 "' WHERE br_fak='" faktura_sif "'"
    ]
    if not mysts [ return false ]
    return true
  ]
  
  novi_red_troska: func [/local row1 n1 ]
  [
    if not write_mode [ alert "Nije dozvoljeno!" exit ]
    mysql_cmd
    [
      "SELECT MAX(rbr) AS broj FROM kalk_dokumenta WHERE br_dok='" rek/sif_dok "' "
    ]
    row1: get_rekord first db
    n1: int_value row1/broj
    n1: n1 + 1
    mysql_cmd
    [
      "INSERT IGNORE INTO kalk_dokumenta SET br_dok='" rek/sif_dok "',tip='TR',rbr='" n1 "' "
    ]
    if not mysts [ exit ]
    mod_faktura
    mysql_cmd
    [
      "SELECT * FROM kalk_dokumenta WHERE br_dok='" rek/sif_dok "' AND tip='TR' AND rbr='" n1 "' "
    ]
    row1: get_rekord first db
    if retval [ ablak_troska row1 ]
  ]
  
  ablak_troska: func [ row1[object!] ]
  [
    tabla_blk: compose
    [
      size 560x120
      backdrop color_g6
      across
      at 9x9 button close_btn_img 26x26
      [
        mod_faktura
        hide-popup
      ]
      at 40x10 h3 "Dodatni trošak:" 150x24
      at 300x10 button "Brisati" 80x24 color_m4
      [
        if not write_mode [ alert "Nije dozvoljeno!" exit ]
        mysql_cmd
        [
          "DELETE FROM kalk_dokumenta WHERE br_dok='" rek/sif_dok "' AND mbr='" row1/mbr "' "
        ]
        mod_faktura
        hide-popup
      ]
      ; --- bx ---
      at 10x40 text right "Opis:" 90x24
      at 100x40 field row1/naziv 450x24 
      at 10x80 text right "Cena:" 90x24
      at 100x80 field row1/jed_cena 100x24 
      at 300x80 button "Modif" 80x24 color_m2
      [
        if not write_mode [ alert "Nije dozvoljeno!" exit ]
        mysql_cmd
        [
          "UPDATE kalk_dokumenta SET naziv='" row1/naziv "',jed_cena='" row1/jed_cena "' "
          "WHERE br_dok='" rek/sif_dok "' AND mbr='" row1/mbr "' "
        ]
        mod_faktura
        hide-popup
      ]
    ]
    ablak_mp: layout/offset tabla_blk 0x0
    ;
    inform/title ablak_mp "Dodatni trošak"
  ]

  faktura_kiiras: func [ /local sz1 i1 b1 ]
  [
    if not write_mode [ return true ]
    if rek/faza > "0"
    [
      mysql_cmd
      [
        "UPDATE faktura2 SET klauz='" rek/klauz "',zaglav='" rek/zaglav "',po_nar='" rek/po_nar "',sak_cenu='" rek/sak_cenu "',"
        "dod_mat='" rek/dod_mat "',por_broj='" rek/por_broj "',izb_cene='" rek/izb_cene "',car_tarif='" rek/car_tarif "',"
        "best_nr='" rek/best_nr "',best_bk='" rek/best_bk "',serial_nr='" rek/serial_nr "',lista_banke='" rek/lista_banke "',"
        "kor_modif='" rek/kor_modif "',dat_modif='" rek/dat_modif "' "
        "WHERE br_fak='" faktura_sif "' "
      ]
      return true
    ]
    ;
    if rek/dat_istupa <> zero_datum
    [
      if rek/dat_istupa < "2000"   [ alert "Datum nije zadat!" return true ]
      if rek/dat_istupa < rek/date [ alert "Datum istupa manje od datum faktura!" return true ] 
      rek/faza: copy "G"
    ]
    ;
    rek/dat_modif: sistime/isodate
    rek/kor_modif: korisnik
    sz1: make string! 1000
    for i1 0 19 1
    [
      b1: copy "dod_tr" if i1 > 0 [ append b1 to-string i1 ]
      append sz1 rejoin [ b1 "=" apjel (get in rek to-word b1) apjel "," ]
      b1: copy "dod_c"  if i1 > 0 [ append b1 to-string i1 ]
      append sz1 rejoin [ b1 "=" apjel (get in rek to-word b1) apjel "," ]
    ]
    mysql_cmd
    [
      "UPDATE faktura2 SET date='" rek/date "',sif_dok='" rek/sif_dok "',user='" rek/user "',payment='" rek/payment "',transport='" rek/transport "',"
      "delivery='" rek/delivery "',pakovanje='" rek/pakovanje "',veza_dok='" rek/veza_dok "',neto='" rek/neto "',bruto='" rek/bruto "',"
      "sif_mag='" rek/sif_mag "',opis='" rek/opis "',otp_valuta='" rek/otp_valuta "',otp_kurs='" rek/otp_kurs "',predracun='" rek/predracun "',"
      "dat_avans='" rek/dat_avans "',avans='" rek/avans "',obrac_pdv='" rek/obrac_pdv "',klauz='" rek/klauz "',zaglav='" rek/zaglav "',"
      "po_nar='" rek/po_nar "',sak_cenu='" rek/sak_cenu "',dod_mat='" rek/dod_mat "',por_broj='" rek/por_broj "'," sz1 "izb_cene='" rek/izb_cene "',"
      "car_tarif='" rek/car_tarif "',best_nr='" rek/best_nr "',best_bk='" rek/best_bk "',serial_nr='" rek/serial_nr "',dat_istupa='" rek/dat_istupa "',"
      "dat_val='" rek/dat_val "',faza='" rek/faza "',lista_banke='" rek/lista_banke "',zbir_fakt='" rek/zbir_fakt "',"
      "kor_modif='" rek/kor_modif "',dat_modif='" rek/dat_modif "' "
      "WHERE br_fak='" faktura_sif "' "
    ]
    ;-- frissitem a prodaja serijat
    if rek/veza_dok <> ""
    [
      blk5: parse/all rek/veza_dok "."
      if (length? blk5) = 2
      [
        mysql_cmd
        [ 
          "UPDATE prodaja_serija SET faza_ser='Zat',br_fak='" faktura_sif "' "
          "WHERE br_prod='" blk5/1 "' AND serija='" blk5/2 "' "
        ]
      ]
    ]
    return false
  ]

  ulfakt_panel_rajzolo: func [ p1[pair!] /local p2 blk_upl ino_upl uk_upl uk_uplata saldo saldo0 tar2 row1 row2 blk0 blk1 sz0 filt1 ]
  [
    blk0: make block! [[][]]
    blk1: make block! []
    br_fak:  copy ""
    dat_fak: copy "" 
    sz0:     copy ""
    filt1:   none
    ;
    p1: p1 + 60x30  ; panel start
    p2: 10x80       ; panel size minimum
    ;
    blk_ulfak: compose/only
    [
      size p2 across
      style t_nev text right 90x24
      style t_fld field 120x24
      style t_chk check color_g2 with [ edge: [ color: coal size: 1x1 ] ]
      style tlab text with [ edge: [color: coal size: 1x1 ] font: [ space: 0x-2 ] para: [ origin: 1x2 ] ]
      backdrop color_g6
      ;
      at  10x10 t_nev left bold "Povezane ulazne raèune:" 200x24
      at   0x40 text right "Ulaz.fakt:" 70x24
      at  70x40 arrow right 24x24 color_l2
      [
        tabla_keres "izbor_ulazne_fakture"
        tabla/value: none
        izbor_sifre  none
        if retval
        [
          mode1/br_fak:  copy retval/1
          mysql_cmd ["SELECT datum FROM dok_ulfakt WHERE br_fak='" mode1/br_fak "'"]
          if empty? row1: first db [ alert "Ulazna faktura nije u bazi!" exit ]
          mode1/dat_fak: copy row1/1/1
          mod_faktura
        ]
      ]
      at 100x40 info center (mode1/br_fak)     110x24 color_g2
      at 200x40 text right "Datum fakt:"       100x24
      at 300x40 info center (mode1/dat_fak)    100x24 color_g2
      at 410x40 btn "Novi red" 80x24 color_t2
      [
        if not write_mode [ alert "Nije dozvoljeno!" exit ]
        append mode1/lista_fak mode1/br_fak
        sz0: blokk_pakolo mode1/lista_fak " "
        mysql_cmd
        [
          "UPDATE faktura2 AS t1 SET t1.lista_ulfak='" sz0 "' WHERE t1.br_fak='" rek/br_fak "'"
        ]
        mode1/br_fak:  copy ""
        mode1/dat_fak: copy ""
        mod_faktura
      ]
      space -1x0
      at (p2)
      tlab center "Br.fakt."    110x25 color_h1 color_h2
      tlab center "Dat.fakt."    90x25 color_h1 color_h2
      tlab center "Iznos fakt"   95x25 color_h1 color_h2
      tlab center "Part."        50x25 color_h1 color_h2
      tlab center "Naziv part." 150x25 color_h1 color_h2
    ]
    p2: p2 + 0x24
    ;
    if rek/lista_ulfak <> ""
    [
      mode1/lista_fak: parse rek/lista_ulfak none
    ]
    filt1: mylist_form mode1/lista_fak
    mysql_cmd compose
    [
      "SELECT t1.br_fak,t1.datum,t1.sif_part,t1.naz_part,t1.iznos "
      "FROM dok_ulfakt AS t1 "
      "WHERE t1.br_fak IN (" (filt1) ")"
    ]
    tar2: get_rekordset copy db
    foreach row2 tar2
    [
      append blk_ulfak compose/only
      [
        at (p2)
        tlab center (row2/br_fak)    110x24 color_b1 color_b2
        with (compose/only [ data: (row2) ])
        [
          if not write_mode [ alert "Nije dozvoljeno!" exit ]
          row2: face/data
          if (request/confirm rejoin [ "Brisati ul.fakture " (row2/br_fak) "?" ]) <> true [ exit ]
          remove find mode1/lista_fak row2/br_fak
          sz0: blokk_pakolo mode1/lista_fak " "
          mysql_cmd
          [
            "UPDATE faktura2 AS t1 SET t1.lista_ulfak='" sz0 "' WHERE t1.br_fak='" rek/br_fak "'"
          ]
          rek/lista_ulfak: copy sz0
          mode1/br_fak:    copy ""
          mode1/dat_fak:   copy ""
          mod_faktura
        ]
        tlab center (row2/datum)      90x24 color_t1 color_t2
        tlab right  (money_form row2/iznos 2) 95x24 color_t1 color_t2
        tlab center (row2/sif_part)   50x24 color_t1 color_t2
        tlab left para [ wrap?: false ] (row2/naz_part) 150x24 color_t1 color_t2
      ]
      p2: p2 + 0x23
    ]
    p2: p2 + 500x20
    blk0/1: blk_ulfak
    blk0/2: p2
    return blk0
  ]
  
  uplate_panel_rajzolo: func [ p1[pair!] god0[string!] /local p2 blk_upl ino_upl uk_upl uk_uplata saldo saldo0 tar2 row2 blk0 plata_rok plata_dfak sz1 ]
  [
    blk0: make  block! [[][]]
    uk_uplata:  0.0
    saldo:      0.0
    ino_upl:    0.0
    uk_upl:     0.0
    plata_rok:  0.0
    plata_dfak: 0.0
    sz1: copy ""
    ;
    p1: p1 + 60x30 ; panel start
    p2: 10x100 ; panel size minimum
    blk_upl: compose
    [
      size p2 across
      style t_nev text right 90x24
      style t_fld field 120x24
      style t_chk check color_g2 with [ edge: [ color: coal size: 1x1 ] ]
      style tlab text with [ edge: [color: coal size: 1x1 ] font: [ space: 0x-2 ] para: [ origin: 1x2 ] ]
      backdrop color_g6
      ;
      at  10x10 t_nev left bold "Evidencija uplate:" 200x24
      at  10x40 t_nev left "Datum pl." 120x24 
      at 140x40 t_nev left "Iznos"     120x24
      at  10x70 t_date: w_date (sistime/isodate) 120x24
      at 140x70 t_iznos: field 120x24
      at 280x70 btn "Novi red" 120x24 color_t2
      [
        if not write_mode [ alert "Nije dozvoljeno!" exit ]
        if (t_iznos/text = "0") or (t_iznos/text = "") or ((dec_value rek/uk_izn) <= 0) [ alert "Nije dozvoljeno" exit ] 
        if ((dec_value uk_uplata) + (dec_value t_iznos/text)) >= (dec_value rek/uk_izn)
        [
          rek/faza: copy "G"
          mysql_cmd [ "UPDATE faktura2 SET faza='" rek/faza "' WHERE br_fak='" faktura_sif "' " ]
        ]
        mysql_cmd
        [
          "INSERT INTO upl_fakt SET br_fak='" faktura_sif "',datum='" t_date/text "',upl_eur='" dec_value t_iznos/text "',tip='I',"
          "sif_part='" rek/user "',vrsta='R',opis='" rek/opis "',godina='" god0 "',kurs='" firma_rek/kurs_euro "' "
        ]
        ;
        mysql_cmd
        [
          "SELECT SUM(uplata) AS uk_upl,SUM(upl_eur) AS ino_upl "
          "FROM upl_fakt WHERE br_fak='" faktura_sif "' "
        ]
        if not empty? row3: first db 
        [ 
          uk_upl:  dec_value row3/1/1 
          ino_upl: dec_value row3/1/2 
        ]
        ;
        if (copy/part rek/date 4) = baze_sif
        [
          mysql_cmd
          [
            "UPDATE dok_fakt SET uk_upl='" uk_upl "',ino_upl='" ino_upl "' WHERE br_fak='" faktura_sif "' "
          ]
        ]
        mod_faktura
        ablak_brajz
      ]
      space -1x0
      at 10x100
      tlab center "Datum" 110x38 color_h1 color_h2
      tlab center "Iznos" 120x38 color_h1 color_h2
      tlab center "Saldo" 120x38 color_h1 color_h2
      tlab center as-is "Plaæen od ^/dat.fakt / rok.pl" 140x38 color_h1 color_h2
    ]
    mysql_cmd
    [
      "SELECT mbr,datum,upl_eur,tip,sif_part FROM upl_fakt "
      "WHERE br_fak='" rek/br_fak "' AND vrsta<>'U' AND vrsta<>'P'"
    ]
    tar2: get_rekordset copy db
    p2: p2 + 0x37
    saldo0: (dec_value rek/uk_izn)
    foreach row2 tar2
    [
      uk_uplata: (dec_value uk_uplata) + (dec_value row2/upl_eur)
      saldo: saldo0 - (dec_value uk_uplata)
      plata_rok:  (to-date row2/datum) - (to-date rek/date)
      plata_dfak: (to-date row2/datum) - (to-date rek/dat_val)
      sz1: rejoin [ plata_rok " / " plata_dfak " dan" ]
      append blk_upl compose/only
      [
        at (p2)
        tlab center (row2/datum) 110x25 color_b1 color_b2
        with (compose/only [ data: (row2) ])
        [
          if not write_mode [ alert "Nije dozvoljeno!" exit ]
          ;if rek/faza >= "G" [ alert "Nije dozvoljeno!" exit ] ;-- kihuzva 2018-03-26
          row2: face/data
          if (request/confirm rejoin [ "Brisati uplatu " (money_form row2/upl_eur 2) "?" ]) <> true [ exit ]
          mysql_cmd
          [
            "DELETE FROM upl_fakt WHERE br_fak='" rek/br_fak "' AND mbr='" row2/mbr "' AND tip='I' AND vrsta='R' "
          ]
          mod_faktura
        ]
        tlab right (money_form row2/upl_eur 2) 120x25 color_t1 color_t2
        tlab right (money_form saldo 2)        120x25 color_t1 color_t2
        tlab center (sz1)   140x25 color_t1 color_t2
      ]
      p2: p2 + 0x24
    ]
    if empty? tar2 [ saldo: saldo0 ] ; ha nincs uplata, akkor a saldo a teljes tartozas
    append blk_upl compose
    [
      at (p2)
      tlab right "Ukupno" 110x25 color_h1 color_h2
      tlab right (money_form uk_uplata 2) 120x25 color_h1 color_h2
      tlab right (money_form saldo 2) 120x25 color_h1 color_h2
    ]
    append blk_upl compose
    [
      at (p2 +  0x30) t_nev right "Saldo:" 90x24
      at (p2 + 90x30) info right to-string (money_form saldo 2) 150x24 color_g2
      at (p2 +  0x60) text right "Napomena:"  90x24 
      at (p2 + 90x60) area wrap rek/opis 390x48
    ]
    p2: p2 + 500x120
    blk0/1: blk_upl
    blk0/2: p2
    return blk0
  ]
  
  mod_faktura: func [ /local tar1 row0 row1 row2 row3 pnev1 p1 p2 i1 sz1 blk blk1 blk2 blk3 blk4 blk5 blk_upl blk_panel blk_panel2 god0 chk_avans 
                      poz1 uk_neto uk_bruto jci1 jci2 jci3 srek mode2 ]
  [
    blk_panel:  make block! []
    blk_panel2: make block! []
    g_tipjci: reduce
    [
      [ "C1" "C2" "C3" ][ "C1 - redovan izvoz" "C2 - privremeni izvoz" "C3 - razduženje privremenog uvoz" ]
    ]
    p2: 0x0
    ; primalac:
    pnev1: ""
    ;
    god0: copy/part rek/date 4
    if rek/user <> "0"
    [
      mysql_cmd
      [
        "SELECT name1 FROM " firma/data ".partners WHERE sifra='" rek/user "' "
      ]
      if not empty? row1: first db
      [
        pnev1: copy row1/1/1
      ]
    ]
    ;
    chk_avans: false
    ;
    tabla_blk: make block! compose
    [
      size p1 + p2 + 0x230
      style t_nev text right 90x24
      style t_fld field 120x24
      style t_chk check color_g2 with [ edge: [ color: coal size: 1x1 ] ]
      style tlab text with [ edge: [color: coal size: 1x1 ] font: [ space: 0x-2 ] para: [ origin: 1x2 ] ]
      backdrop color_g4
      across space -1x8
      at 9x9 button close_btn_img 26x26
      [
        faktura_sif: copy ""
        ablak_cezar
        ablak_brajz
      ]
      at 40x10 text right "Faktura:" 70x24 at 110x10 info center rek/br_fak 120x24 color_g2
      at 250x10 button "Modif" 80x24 color_m2 keycode 'f5
      [
        if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
        if not rac_dom_iznos [ exit ]
        if faktura_kiiras [ exit ]
        ;
        faktura_sif: copy ""
        ablak_cezar
        ablak_brajz
      ]
      at 350x10 button "Brisanje" 80x24 color_m4
      [
        if not write_mode [ exit ]
        if (rek/sif_dok <> "") and (rek/tip_fak <> "")
        [
          alert "Prvo brisati otpremnicu!" exit
        ]
        if (request/confirm rejoin [ "Brisati fakturu " faktura_sif " ?" ]) <> true [ exit ]
        mysql_cmd [ "DELETE FROM faktura2  WHERE br_fak='" faktura_sif "' " ]
        mysql_cmd [ "DELETE FROM prom_fakt WHERE br_fak='" faktura_sif "' " ]
        mysql_cmd [ "DELETE FROM dok_fakt  WHERE br_fak='" faktura_sif "' " ]
        
        ;-- prodaja serija frissites
        if rek/veza_dok <> ""
        [
          mode2: make object!
          [
            serija:  copy ""
            posao:   copy ""
            promena: copy ""
            opis:    copy ""
          ]
          blk5: parse/all rek/veza_dok "."
          either (length? blk5) = 2
          [
            mysql_cmd
            [
              "SELECT t1.*,(SELECT t2.faza_dok FROM dok_prodaje AS t2 WHERE t2.br_prod=t1.br_prod) AS faza_dok "
              "FROM prodaja_serija AS t1 WHERE t1.br_prod='" blk5/1 "' AND t1.serija='" blk5/2 "' "
            ]
            srek: get_rekord first db
            mode2/serija:  copy srek/serija
            dok_prod: copy srek/br_prod
          ]
          [
            mysql_cmd
            [
              "SELECT t1.*,(SELECT t2.faza_dok FROM dok_prodaje AS t2 WHERE t2.br_prod=t1.br_prod) AS faza_dok "
              "FROM prodaja_serija AS t1 WHERE t1.br_prod='" rek/veza_dok "' AND t1.serija='0' "
            ]
            srek: get_rekord first db
            mode2/serija:  copy srek/serija
            dok_prod: copy srek/br_prod
          ]
          mode2/posao:   copy "Brisanje"
          mode2/promena: copy "Faktura"
          mode2/opis:    rejoin [ "Brisanje fakture: " faktura_sif ]
          prodaja_hist_write mode2
          srek/br_fak: copy ""
          update_serija_prod srek
          dok_prod: none
        ]
        if rek/sif_dok <> ""
        [
          mysql_cmd 
          [ 
            "UPDATE dokument SET br_ul_rac='' WHERE br_dok='" rek/sif_dok "' "
          ]
        ]
        faktura_sif: copy ""
        ablak_cezar
        ablak_brajz
      ]
      at 440x10 text right "Faza:" 60x24
      at 500x10 info center (rek/faza) 30x24 color_g2
    ]
    either rek/faza > "0"
    [
      append tabla_blk compose
      [
        at 535x10 button "Otvori" 60x24 color_m2
        [
          if not write_mode [ alert "Nije dozvoljeno!" exit ]
          if rek/faza > "G" [ alert "Nije dozvoljeno!" exit ]
          rek/faza: copy ""
          mysql_cmd [ "UPDATE faktura2 SET faza='" rek/faza "' WHERE br_fak='" faktura_sif "' " ]
          mod_faktura
        ]
      ]
    ]
    [
      append tabla_blk compose
      [
        at 535x10 button "Gotov" 60x24 color_m2
        [
          if not write_mode [ alert "Nije dozvoljeno!" exit ]
          if rek/faza > "G" [ alert "Nije dozvoljeno!" exit ]
          if rek/dat_istupa = zero_datum [ alert "Datum istupa nije izabran!" exit ]
          if rek/jci_broj = "" [ alert "JCI broj nije upisan!" exit ]
          rek/faza: copy "G"
          mysql_cmd [ "UPDATE faktura2 SET faza='" rek/faza "' WHERE br_fak='" faktura_sif "' " ]
          mod_faktura
        ]
      ]
    ]
    append tabla_blk compose
    [
      at 10x170 t_nev "Payment:" 140x24 
    ]
    ;-- itt nezem hogy mifele a payment
    either (empty? rek/payment) or (rek/payment = "Avansno") 
    [
      ;-- ha Avansno a payment akkor az info van toltve, a date van beirva a dat_val-ba
      ;-- amig nincs date of delivery kivalsztva, az avans-t se lehet bejelolni
      if (rek/payment = "Avansno") and (rek/date/1 > #"0")
      [ 
        chk_avans: true
        rek/payment: copy "Avansno"
      ]
      rek/dat_val: copy rek/date
    ]
    [
      ;-- ha nincs az avans kivalasztva, szamolom a dat.placanja-t, de csak akkor ha megegyezik a date of delivery-vel
      ;-- ha uj szamlat nyitok, akkor alapbol a partners-bol veszi a rok.placanja-t
      if (rek/date/1 > #"0") and (rek/date = rek/dat_val)
      [
        rek/dat_val: to-iso-date ( (to-date rek/date ) + (int_value rek/rok_pl) )
      ]
      rek/rok_pl: to-string ( (to-date rek/dat_val) - (to-date rek/date ) )
      if rek/dat_val/1 > #"0" [ rek/payment: to-reg-date rek/dat_val ]
    ]
    ;
    either chk_avans = true
    [
      append tabla_blk compose
      [
        at 150x170 info rek/payment 280x24 color_g2
      ]
    ]
    [
      append tabla_blk compose
      [
        at 150x170 w_date rek/dat_val 120x24 akcio
        [
          ;-- szamolom hogy mennyi a rok.placanja a beallitott datum alapjan
          rek/rok_pl: to-string ( (to-date rek/dat_val) - (to-date rek/date ) )
          if rek/date/1 > #"0" [ rek/payment: to-reg-date rek/dat_val ]
          mod_faktura
        ]
        at 270x170 text right "Rok.pl.:" 70x24 
        at 340x170 field center rek/rok_pl 40x24
        [
          ;-- a datum placanja kiszamitasa a rok.pl. alapjan ha a date of delivery nem 0000.00.00.
          if rek/date/1 > #"0"
          [
            rek/dat_val: to-iso-date ( (to-date rek/date ) + (int_value rek/rok_pl) )
          ]
          if rek/dat_val/1 > #"0" [ rek/payment: to-reg-date rek/dat_val ]
          mod_faktura
        ] 
        at 380x170 text right "dana" 50x24
      ]
    ]
    append tabla_blk compose
    [
      at 450x170 t_chk chk_avans 24x24 
      [ 
        chk_avans: face/data
        either chk_avans = true [ rek/payment: copy "Avansno" ][ rek/payment: copy paym_dat ]
        mod_faktura 
      ]
      at 480x170 text left "Avansno"  100x24
      at   0x200 t_nev "Mode of transport:" 150x24 at 150x200 t_fld rek/transport 400x24
      at   0x230 t_nev "Terms of delivery:" 150x24 at 150x230 t_fld rek/delivery  400x24
      at   0x260 t_nev "Date of delivery:"  150x24
      at 150x260 w_date rek/date 120x24 akcio 
      [
        ;-- datum kiszamitasa a rok.pl. alapjan
        if rek/date/1 > #"0"
        [
          rek/dat_val: to-iso-date ( (to-date rek/date ) + (int_value rek/rok_pl) )
        ]
        if rek/dat_val/1 > #"0" [ rek/payment: to-reg-date rek/dat_val ]
        mod_faktura
      ]
      at 290x260 btn "Invoice CSV" 100x24 color_l6
      [
        either not none? banka1 
        [ 
          rek/lista_banke: copy banka1
          if not none? banka2 [ append rek/lista_banke rejoin [ "|" banka2 ] ] 
        ]
        [
          if not none? banka2 [ rek/lista_banke: copy banka2 ]
        ]
        if (rek/lista_banke = "") or (rek/lista_banke = "|") [ alert "Fali banke!" exit ]
        faktura_kiiras
        export_invoice_csv (reduce [ rek/br_fak ]) none
      ]
      at 420x260 btn "Štamp.Invoice" 130x24 color_l2
      [
        either not none? banka1 
        [ 
          rek/lista_banke: copy banka1
          if not none? banka2 [ append rek/lista_banke rejoin [ "|" banka2 ] ] 
        ]
        [
          if not none? banka2 [ rek/lista_banke: copy banka2 ]
        ] 
        if (rek/lista_banke = "") or (rek/lista_banke = "|") [ alert "Fali banke!" exit ]
        faktura_kiiras
        stamp_invoice (reduce [ rek/br_fak ]) none
      ]
      at  10x290 t_nev "Neto:" 140x24 at 150x290 t_fld rek/neto
      at 420x290 btn   "Štamp.Liefersch." 130x24 color_l2
      [
        if rek/faza < "G" [ faktura_kiiras ]
        if rek/sak_cenu = "da" [ stamp_liefersch rek   exit ]
        stamp_liefersch/stcena rek
      ]
      at 10x320 t_nev "Bruto:" 140x24 at 150x320 t_fld rek/bruto
      at 290x320 btn "Omot dokum" 100x24 color_l2
      [
        if rek/faza < "G" [ faktura_kiiras ]
        stamp_omota_izvoza rek
      ]
      at 420x320 btn "Nalog za izvoz" 130x24 color_l2
      [
        if rek/faza < "G" [ faktura_kiiras ]
        stamp_nal_izvoz rek/br_fak
      ]
      at 10x350 t_nev "Pakovanje:" 140x24 at 150x350 t_fld rek/pakovanje 400x24
      at 2x358 t_nev "Opis:" 50x24
      at 5x382 area wrap rek/opis 640x43
      at 290x290 btn "Naimenov.JCI" 100x24 color_l6
      [
        xml_naimenov_jci rek
      ]
    ]
    either (rek/tip_fak = "")
    [
      mysql_cmd
      [
        "SELECT t1.br_dok,t1.sif_mag,t1.sif_part,t1.primalac,t2.name1 AS naz_part,t3.name1 AS naz_prima,t1.otp_valuta "
        "FROM dokument AS t1 "
        "LEFT JOIN " firma/data ".partners AS t2 ON t2.sifra=t1.sif_part "
        "LEFT JOIN " firma/data ".partners AS t3 ON t3.sifra=t1.primalac "
        "WHERE t1.br_dok='" rek/sif_dok "' "
      ]
      rek0: get_rekord first db
      either retval
      [
        if none? rek0/naz_part  [ rek0/naz_part: copy "" ]
        if none? rek0/naz_prima [ rek0/naz_prima: copy "" ]
        if (rek0/primalac = "") or (rek0/primalac = "0")
        [
          rek0/primalac:  copy rek/user
          rek0/naz_prima: copy pnev1
        ]
        rek/sif_mag: copy rek0/sif_mag
      ]
      [
        rek/sif_dok: copy ""
      ]
      blk: compose
      [
        size 640x125 across
        backdrop color_g6
        at 0x5 t_nev "Otpremnica:" 110x24 at 110x5 info center rek/sif_dok 120x24 color_g2
        arrow right 24x24 color_l2
        [
          if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
          tabla_keres "dokum_izb"
          tabla/value: reduce [ rek/sif_dok ]
          tabla/filter: copy ""
          set_table_form [ clr "uslov" uslov "t1.br_dok" "M:I/" ]
          izbor_sifre none
          set_table_form [ clr "uslov" ]
          if retval
          [
            rek/sif_dok: copy retval/1
            mysql_cmd
            [
              "SELECT otp_valuta,otp_kurs,veza_dok,sif_mag FROM dokument WHERE br_dok='" rek/sif_dok "' "
            ]
            if not empty? row1: first db
            [
              rek/otp_valuta: copy row1/1/1
              rek/otp_kurs:   copy row1/1/2
              rek/veza_dok:   copy row1/1/3
              rek/sif_mag:    copy row1/1/4
            ]
            faktura_kiiras
            mod_faktura
          ]
        ]
        at 270x5 button "Otpremnica" 100x24 color_l2
        [
          if rek/sif_dok <> ""
          [
            insert nyomtar reduce [ compose [ ~"Ino-Faktura" (faktura_sif) ] ]
            nyomelem: reduce [ rek/sif_dok ]
            program_nev: ~"Knjiženje-mag" program_indito
          ]
        ]
        at 370x5 text right "Mag:" 50x24
        at 420x5 info center rek/sif_mag 50x24 color_g2
        at 0x65 t_nev "User(Invoice):" 110x24 at 110x65 info center (rek/user) 70x24 color_g2
        arrow right 24x24 color_l2
        [
          if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
          knjig_partner_izbor rek/user none "W0"
          if object? retval
          [
            rek/user:   copy retval/sifra
            rek/rok_pl: copy retval/rok_pl
            faktura_kiiras
            mod_faktura
          ]
        ]
        pad 10 text pnev1 350x24
        [
          insert nyomtar reduce [ compose [ ~"Ino-Faktura" (rek/br_fak) ] ]
          nyomelem: reduce [ rek/user ]
          program_nev: ~"Partner" program_indito
        ]
      ]
      if rek/sif_dok <> ""
      [
        append blk compose
        [
          at 0x35 t_nev "Kupac:" 110x24
          at 110x35 info center rek0/sif_part 70x24 color_g2
          pad 10 text rek0/naz_part 350x24
          [
            if rek0/sif_part <> ""
            [
              insert nyomtar reduce [ compose [ ~"Ino-Faktura" (rek/br_fak) ] ]
              nyomelem: reduce [ rek0/sif_part ]
              program_nev: ~"Partner" program_indito
            ]
          ]
          at 0x95 t_nev "Prima(Lief.):" 110x24 at 110x95 info center rek0/primalac 70x24 color_g2
          arrow right 24x24 color_l2
          [
            if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
            if rek/sif_dok <> "" [ alert "Prvo brisati otpremnicu!" exit ]
            knjig_partner_izbor rek0/primalac none "W0"
            if object? retval
            [
              rek0/primalac: copy retval/sifra
              sz1: copy ""
              if rek0/primalac <> ""
              [
                mysql_cmd
                [
                  "SELECT t1.mesto,t1.ulica,t1.drzava FROM " firma/data ".partners AS t1 WHERE t1.sifra='" rek0/primalac "' "
                ]
                row2: get_rekord first db
                sz1: rejoin [ row2/ulica ", " row2/mesto ]
                if row2/drzava <> ""
                [
                  append sz1 rejoin [ " " row2/drzava ]
                ]
              ]
              mysql_cmd
              [
                "UPDATE dokument AS t1 SET t1.primalac='" rek0/primalac "',t1.mesto_isp='" sz1 "' "
                "WHERE t1.br_dok='" rek/sif_dok "' "
              ]
              faktura_kiiras
              mod_faktura
            ]
          ]
          pad 10 text rek0/naz_prima 350x24
          [
            if rek0/primalac <> ""
            [
              insert nyomtar reduce [ compose [ ~"Ino-Faktura" (rek/br_fak) ] ]
              nyomelem: reduce [ rek0/primalac ]
              program_nev: ~"Partner" program_indito
            ]
          ]
        ]
      ]
    ]
    [
      mysql_cmd
      [
        "SELECT t1.br_dok,t1.sif_mag,t1.sif_part,t1.primalac,t2.name1 AS naz_part,t3.name1 AS naz_prima,t1.otp_valuta "
        "FROM dokument AS t1 "
        "LEFT JOIN " firma/data ".partners AS t2 ON t2.sifra=t1.sif_part "
        "LEFT JOIN " firma/data ".partners AS t3 ON t3.sifra=t1.primalac "
        "WHERE t1.br_dok='" rek/sif_dok "' "
      ]
      rek0: get_rekord first db
      either retval
      [
        if none? rek0/naz_part  [ rek0/naz_part: copy "" ]
        if none? rek0/naz_prima [ rek0/naz_prima: copy "" ]
        if (rek0/primalac = "") or (rek0/primalac = "0")
        [
          rek0/primalac:  copy rek/user
          rek0/naz_prima: copy pnev1
        ]
        rek/sif_mag: copy rek0/sif_mag
      ]
      [
        rek/sif_dok: copy ""
      ]
      ; globalis faktura:
      mysql_cmd
      [
        "SELECT t1.sif_part,t1.primalac,t2.name1 AS naz_part,t3.name1 AS naz_prima,"
        "(SELECT t4.br_dok FROM dokument AS t4 WHERE t4.br_dok='" rek/sif_dok "') AS br_dok "
        "FROM dok_fakt AS t1 "
        "LEFT JOIN " firma/data ".partners AS t2 ON t2.sifra=t1.sif_part "
        "LEFT JOIN " firma/data ".partners AS t3 ON t3.sifra=t1.primalac "
        "WHERE t1.br_fak='" rek/br_fak "' "
      ]
      rek1: get_rekord first db
      blk: compose
      [
        size 640x125 across
        backdrop color_g6
        at 0x5 t_nev "Otpremnica:" 110x24 
        at 110x5 arrow right 24x24 color_l2
        [
          if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
          tabla_keres "dokum_izb"
          tabla/value: reduce [ rek/sif_dok ]
          tabla/filter: copy ""
          set_table_form [ clr "uslov" uslov "t1.br_dok" "M:I/" ]
          izbor_sifre [ 'btn "Bris.vezu" ]
          set_table_form [ clr "uslov" ]
          if retval = "Bris.vezu"
          [
            rek/sif_dok: copy ""
            faktura_kiiras
            mod_faktura
          ]
          if block? retval
          [
            rek/sif_dok: copy retval/1
            mysql_cmd
            [
              "SELECT otp_valuta,otp_kurs FROM dokument WHERE br_dok='" rek/sif_dok "' "
            ]
            if not empty? row1: first db
            [
              rek/otp_valuta: copy row1/1/1
              rek/otp_kurs: copy row1/1/2
            ]
            faktura_kiiras
            mod_faktura
          ]
        ]
        at 140x5 info center rek/sif_dok 120x24 color_g2
        at 370x5 text right "Mag:" 50x24
        at 420x5 info center rek/sif_mag 40x24 color_g2
        at 0x65 t_nev "User(Invoice):" 110x24 
        at 110x65 arrow right 24x24 color_l2
        [
          if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
          knjig_partner_izbor rek/user none "W0"
          if object? retval
          [
            rek/user: copy retval/sifra
            faktura_kiiras
            mod_faktura
          ]
        ]
        at 140x65 info center (rek/user) 70x24 color_g2
        at 210x65 text pnev1 350x24
        [
          insert nyomtar reduce [ compose [ ~"Ino-Faktura" (rek/br_fak) ] ]
          nyomelem: reduce [ rek/user ]
          program_nev: ~"Partner" program_indito
        ]
        at 0x35 t_nev "Kupac:" 110x24 
        at 110x35 arrow right 24x24 color_l2
        [
          if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
          knjig_partner_izbor rek1/sif_part none "W0"
          if object? retval
          [
            rek1/sif_part: copy retval/sifra
            mysql_cmd
            [
              "UPDATE dok_fakt SET sif_part='" rek1/sif_part "' WHERE br_fak='" rek/br_fak "' "
            ]
            faktura_kiiras
            mod_faktura
          ]
        ]
        at 140x35 info center rek1/sif_part 70x24 color_g2
        at 210x35 text rek1/naz_part 350x24
        [
          if rek1/sif_part <> ""
          [
            insert nyomtar reduce [ compose [ ~"Ino-Faktura" (rek/br_fak) ] ]
            nyomelem: reduce [ rek1/sif_part ]
            program_nev: ~"Partner" program_indito
          ]
        ]
        at 0x95 t_nev "Prima(Lief.):" 110x24 
        at 110x95 arrow right 24x24 color_l2
        [
          if rek/faza > "0"    [ alert "Faktura zatvorena!"       exit ]
          if rek/sif_dok <> "" [ alert "Prvo brisati otpremnicu!" exit ]
          knjig_partner_izbor rek1/primalac none "W0"
          if object? retval
          [
            rek1/primalac: copy retval/sifra
            mysql_cmd
            [
              "UPDATE dok_fakt SET primalac='" rek1/primalac "',mesto_isp='" retval/mesto "',ulica_isp='" retval/ulica "' WHERE br_fak='" rek/br_fak "' "
            ]
            if object? rek0
            [
              rek0/primalac: copy retval/sifra
              sz1: copy ""
              if rek0/primalac <> ""
              [
                mysql_cmd
                [
                  "SELECT t1.mesto,t1.ulica,t1.drzava FROM " firma/data ".partners AS t1 WHERE t1.sifra='" rek0/primalac "' "
                ]
                row2: get_rekord first db
                sz1: rejoin [ row2/ulica ", " row2/mesto ]
                if row2/drzava <> ""
                [
                  append sz1 rejoin [ " " row2/drzava ]
                ]
              ] 
              mysql_cmd
              [
                "UPDATE dokument SET primalac='" rek0/primalac "',mesto_isp='" sz1 "' WHERE br_dok='" rek/sif_dok "' "
              ]
            ]
            faktura_kiiras
            mod_faktura
          ]
        ]
        at 140x95 info center rek1/primalac 70x24 color_g2
        at 210x95 text rek1/naz_prima 350x24
        [
          if rek1/primalac <> ""
          [
            insert nyomtar reduce [ compose [ ~"Ino-Faktura" (rek/br_fak) ] ]
            nyomelem: reduce [ rek1/primalac ]
            program_nev: ~"Partner" program_indito
          ]
        ]
      ]
      if (write_mode) and (rek/sif_dok = "")
      [
        append blk compose
        [
          at 270x5 button "Kreir.Otpr." 100x24 color_m2
          [
            if rek1/primalac = "" [ alert "Prvo izaberi primalac!" exit ]
            if not write_mode [ alert "Nije dozvoljeno!"   exit ]
            if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
            faktura_kiiras
            mysql_cmd
            [
              "SELECT * FROM dok_fakt WHERE br_fak='" rek/sif_fak "' "
            ]
            row1: get_rekord first db
            if not retval 
            [ 
              alert "Univerzalna faktura nije upisana!" 
              ablak_cezar
              ablak_brajz
              exit 
            ]
            row1/param: parse (at row1/param 2) "=|"
            ; otpremnica datum:
            row1/datum_dok: copy rek/date
            row1/sif_mag: copy rek/sif_mag
            if object? rek2
            [
              row1/valuta: copy rek2/valuta
            ]
            mysql_cmd
            [
              "UPDATE dok_fakt SET datum='" rek/date "',br_dok='" rek/sif_dok "',"
              "datum_dok='" row1/datum_dok "',sif_mag='" row1/sif_mag "',valuta='" row1/valuta "' "
              "WHERE br_fak='" rek/sif_fak "' "
            ]
            kreiranje_otpremnice row1
            ;
            rek/sif_dok: copy row1/br_dok
            faktura_kiiras
            ;-- prodaja serija frissites
            if rek/veza_dok <> ""
            [
              mode2: make object!
              [
                serija:  copy ""
                posao:   copy ""
                promena: copy ""
                opis:    copy ""
              ]
              blk5: parse/all rek/veza_dok "."
              either (length? blk5) = 2
              [
                mysql_cmd
                [
                  "SELECT t1.*,(SELECT t2.faza_dok FROM dok_prodaje AS t2 WHERE t2.br_prod=t1.br_prod) AS faza_dok "
                  "FROM prodaja_serija AS t1 WHERE t1.br_prod='" blk5/1 "' AND t1.serija='" blk5/2 "' "
                ]
                srek: get_rekord first db
                mode2/serija:  copy srek/serija
                dok_prod: copy srek/br_prod
              ]
              [
                mysql_cmd
                [
                  "SELECT t1.*,(SELECT t2.faza_dok FROM dok_prodaje AS t2 WHERE t2.br_prod=t1.br_prod) AS faza_dok "
                  "FROM prodaja_serija AS t1 WHERE t1.br_prod='" rek/veza_dok "' AND t1.serija='0' "
                ]
                srek: get_rekord first db
                mode2/serija:  copy srek/serija
                dok_prod: copy srek/br_prod
              ]
              mode2/posao:   copy "Kreiranje otpremnice"
              mode2/promena: copy "Otpremnica"
              mode2/opis:    rejoin [ "Kreiranje otpremnice: " rek/sif_dok ]
              prodaja_hist_write mode2
              srek/faza_ser: copy "Zat"
              srek/br_fak:   copy rek/sif_fak
              update_serija_prod srek
              dok_prod: none
            ]
            mod_faktura
          ]
        ]
      ]
      if rek/sif_dok <> ""
      [
        append blk compose
        [
          at 270x5 button "Otpremnica" 100x24 color_l2
          [
            if rek/sif_dok <> ""
            [
              insert nyomtar reduce [ compose [ ~"Ino-Faktura" (faktura_sif) ] ]
              nyomelem: reduce [ rek/sif_dok ]
              program_nev: ~"Knjiženje-mag" program_indito
            ]
          ]
        ]
      ]
      if find [ "prg" "adm" "sis" ] dozvola
      [
        append tabla_blk compose
        [
          at 560x200 button "Promet" 80x24 color_b2
          [
            if not write_mode [ alert "Nije dozvoljeno!" exit ]
            if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
            faktura_kiiras
            mysql_cmd
            [
              "SELECT * FROM dok_fakt WHERE br_fak='" rek/sif_fak "' "
            ]
            rek: get_rekord first db
            if not retval 
            [ 
              alert "Univerzalna faktura nije upisana!" 
              ablak_cezar
              ablak_brajz
              exit 
            ]
            rek/param: parse (at rek/param 2) "=|"
            faktura_fprom
          ]
        ]
      ]
    ]
    append tabla_blk compose
    [
      at 5x38 panel blk with [ edge: [color: coal size: 1x1 ]]
    ]
    p1: 0x430
    append tabla_blk compose
    [
      at (p1) text right "Inserted:" 80x24
      at (p1 + 80x0)  info center rek/kor_insert 70x24 color_g2
      at (p1 + 150x0) info center rek/dat_insert 100x24 color_g2
      at (p1 + 250x0) text right "Modified:" 90x24
      at (p1 + 340x0) info center rek/kor_modif 70x24 color_g2
      at (p1 + 410x0) info center rek/dat_modif 100x24 color_g2
      at (p1 + 510x0) text right "Tip:" 40x24
      at (p1 + 550x0) info center rek/tip_fak 40x24 color_g2
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Dokument veze:" 140x24 
      at (p1 + 140x0) arrow right 24x24 color_l2
      [
        if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
        tabla_keres "prodaja_izbor"
        blk5: parse/all rek/veza_dok "."
        either (length? blk5) = 2
        [
          tabla/value: reduce [ blk5/1 blk5/2 ]
          tabla/filter: rejoin [ "serija=" apjel (blk5/2) apjel ]
          izbor_sifre [ 'btn ~"Brisati vezu" ]
          if none? retval [ exit ]
          if block? retval [ rek/veza_dok: rejoin [ retval/1 "." retval/2 ] ]
        ]
        [
          tabla/value: reduce [ rek/veza_dok "0" ]
          tabla/filter: rejoin [ "serija=" apjel "0" apjel ]
          izbor_sifre [ 'btn ~"Brisati vezu" ]
          if none? retval [ exit ]
          if block? retval [ rek/veza_dok: rejoin [ retval/1 ".0" ] ]
        ]
        if retval = ~"Brisati vezu" [ rek/veza_dok: copy "" ]
        faktura_kiiras
        mod_faktura
      ]
      at (p1 + 170x0) info center rek/veza_dok 170x24 color_g2
      at (p1 + 340x0) btn "Dok" 40x24 color_l2
      [
        if rek/veza_dok = "" [ alert "Izabrati dokument prodaje!" exit ]
        insert/only nyomtar reduce [ ~"Ino-Faktura" faktura_sif ]
        blk5: parse/all rek/veza_dok "."
        either (length? blk5) = 2
        [
          nyomelem: reduce [ blk5/1 blk5/2 ]
          mysql_cmd [ "SELECT grupa FROM dok_prodaje WHERE br_prod='" blk5/1 "' " ]
          row0: get_rekord first db
        ]
        [
          nyomelem: reduce [ rek/veza_dok "0" ]
          mysql_cmd [ "SELECT grupa FROM dok_prodaje WHERE br_prod='" rek/veza_dok "' " ]
          row0: get_rekord first db
        ]
        either row0/grupa = "DP"
        [
          program_nev: ~"Dokum-Prodaje" program_indito
        ]
        [
          program_nev: ~"Evidencija-Prodaje" program_indito
        ]
      ]
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Avansni raèun:" 140x24 at (p1 + 140x0) t_fld rek/predracun 120x24
      at (p1 + 260x0) t_nev "od:" 40x24 at (p1 + 300x0) w_date rek/dat_avans 120x24
      at (p1 + 420x0) t_nev "Iznos:" 70x24 at (p1 + 490x0) t_fld rek/avans 100x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1 + 5x0) box with [ edge: [color: coal size: 1x1 ]] 635x65 color_g6
    ]
    ;JCI broj
    jci1: copy "" 
    jci2: copy "" 
    jci3: copy "" 
    blk4: parse/all rek/jci_broj "-"
    jci2: copy rek/jci_broj
    if (length? blk4) = 2
    [
      jci1: copy blk4/1
      jci2: copy blk4/2
    ]
    p1: p1 + 0x5
    append tabla_blk compose
    [
      at (p1) t_nev "Datum istupa:" 140x24 at (p1 + 140x0) w_date rek/dat_istupa 120x24
      at (p1 + 260x0) text right  "JCI oznaka:" 100x24
      at (p1 + 360x0) drop-down wrap data g_tipjci/2 250x24
      with [ rows: 4 text: (blk_find g_tipjci/1 g_tipjci/2 jci1) ]
      [
        if string? face/data
        [
          jci1: blk_find g_tipjci/2 g_tipjci/1 face/text
          t_ozn/text: copy jci1
          show t_ozn
        ]
      ]
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) text right  "JCI broj:" 140x24
      at (p1 + 140x0) t_ozn: info center (jci1) 50x24 color_g2
      at (p1 + 190x0) text center  " - " 25x24
      at (p1 + 215x0) w_field (jci2) 80x24  
      at (p1 + 360x0) btn "Upis-JCI br." 100x24 color_f4
      [
        if jci2 = "" [ alert "Upisati broj JCI!" exit ]
        ;
        rek/jci_broj: copy jci2
        if jci1 <> "" [ rek/jci_broj: rejoin [ jci1 "-" jci2 ] ]
        ;
        if rek/dat_istupa <> zero_datum
        [
          if rek/dat_istupa < "2000" [ alert "Datum nije zadat!" return true ]
          if rek/dat_istupa < rek/date [ alert "Datum istupa manje od datum faktura!" return true ] 
          rek/faza: copy "G"
        ]
        mysql_cmd
        [
          "UPDATE faktura2 SET jci_broj='" rek/jci_broj "',dat_istupa='" rek/dat_istupa "',faza='" rek/faza "' "
          "WHERE br_fak='" faktura_sif "' "
        ]
        if not mysts [ alert "Greška kod upisa baze!" exit ]
        switch jci1
        [
          "C1" [ jci3: copy "Upisan je JCI broj za redovan izvoz: " ]
          "C2" [ jci3: copy "Upisan je JCI broj za privremeni izvoz: " ]
          "C3" [ jci3: copy "Upisan je JCI broj za razduženje privremenog uvoza: " ]
        ]
        mod_faktura
      ]
      at (p1 + 510x0) btn "Bris-JCI br." 100x24 color_m4
      [
        rek/jci_broj: copy ""
        mysql_cmd
        [
          "UPDATE faktura2 SET jci_broj='" rek/jci_broj "' WHERE br_fak='" faktura_sif "' "
        ]
        if not mysts [ alert "Greška kod upisa baze!" exit ]
        mod_faktura
      ]
    ]
    p1: p1 + 0x40
    append tabla_blk compose
    [
      at (p1) t_nev "Dodati klauzulu:" 160x24 at (p1 + 160x0) check (rek/klauz = "new") 24x24
      [
        either face/data [ rek/klauz: copy "new" ][ rek/klauz: copy "" ]
      ]
      at (p1 + 190x0) text "(kod Invoice: THE EXPORTER OF THE PRODUCTS...)" 450x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Dodati por.broj:" 160x24 at (p1 + 160x0) check (rek/por_broj = "da") 24x24
      [
        either face/data [ rek/por_broj: copy "da" ][ rek/por_broj: copy "" ]
      ]
      at (p1 + 190x0) text "(x: dodati poreski broj-Invoice)" 450x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Staviti zaglavlje:" 160x24 at (p1 + 160x0) check (rek/zaglav = "da") 24x24
      [
        either face/data [ rek/zaglav: copy "da" ][ rek/zaglav: copy "" ]
      ]
      at (p1 + 190x0) text "(x: na svaku stranu veliko zaglavlje-Liefersch.)" 450x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Narudžbe posebno:" 160x24 at (p1 + 160x0) check (rek/po_nar = "da") 24x24
      [
        either face/data [ rek/po_nar: copy "da" ][ rek/po_nar: copy "" ]
      ]
      at (p1 + 190x0) text "(x: svaka narudžba na posebnu stranu-Liefersch.)" 450x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Sakriti cenu:" 160x24 at (p1 + 160x0) check (rek/sak_cenu = "da") 24x24
      [
        either face/data [ rek/sak_cenu: copy "da" ][ rek/sak_cenu: copy "" ]
      ]
      at (p1 + 190x0) text "(x: Sakriti cenu kad štamp. Liefersch.)" 450x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Dodati Trg.robu:" 160x24 at (p1 + 160x0) check (rek/dod_mat = "D") 24x24
      [
        either face/data [ rek/dod_mat: copy "D" ][ rek/dod_mat: copy "" ]
      ]
      at (p1 + 190x0) text "(x: dodati sastav trgov.robe)" 450x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Dodati obraè.pdv:" 160x24 at (p1 + 160x0) check (rek/obrac_pdv = "da") 24x24
      [
        either face/data [ rek/obrac_pdv: copy "da" ][ rek/obrac_pdv: copy "" ]
      ]
      at (p1 + 190x0) text "(x: dodati ubraèunat pdv.)" 450x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Dodati car.tarifu:" 160x24 at (p1 + 160x0) check (rek/car_tarif = "da") 24x24
      [
        either face/data [ rek/car_tarif: copy "da" ][ rek/car_tarif: copy "" ]
      ]
      at (p1 + 190x0) text "(x: dodati carinsku tarifu)" 450x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Dodati Best.Nr.:" 160x24 at (p1 + 160x0) check (rek/best_nr = "da") 24x24
      [
        either face/data [ rek/best_nr: copy "da" ][ rek/best_nr: copy "" ]
      ]
      at (p1 + 190x0) text "(x: dodati best. nr.)" 450x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Best.Nr.barkod:" 160x24 at (p1 + 160x0) check (rek/best_bk = "da") 24x24
      [
        either face/data [ rek/best_bk: copy "da" ][ rek/best_bk: copy "" ]
      ]
      at (p1 + 190x0) text "(x: dodati best. nr. barkod)" 450x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Dodati Serial nr.:" 160x24 at (p1 + 160x0) check (rek/serial_nr = "da") 24x24
      [
        either face/data [ rek/serial_nr: copy "da" ][ rek/serial_nr: copy "" ]
      ]
      at (p1 + 190x0) text "(x: dodati serial nr.)" 450x24
    ]
    ;-- majd folytatom - csa 2020.08.11.
    ;p1: p1 + 0x30
    ;append tabla_blk compose
    ;[
    ;  at (p1) t_nev "Deo niža nivo:" 160x24 at (p1 + 160x0) check (rek/best_bk = "da") 24x24
    ;  [
    ;    either face/data [ rek/best_bk: copy "da" ][ rek/best_bk: copy "" ]
    ;  ]
    ;  at (p1 + 190x0) text "(x: dodati niža nivo, samo za deo-Invoice)" 450x24
    ;]
    ;
    if rek/lista_banke = "" [ rek/lista_banke: copy "BINT|BOTP" ]
    blk1: parse/all rek/lista_banke "|"
    while [ (length? blk1) < 2 ]
    [
      append blk1 ""
    ]
    if (length? blk1) = 2 [ banka1: copy blk1/1 banka2: copy blk1/2 ] 
    ;
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) t_nev "Bank1:" 160x24 at (p1 + 160x0) check (banka1 = "BINT") 24x24
      [
        either face/data = true [ banka1: copy "BINT" ][ banka1: copy "" ]
      ]
      at (p1 + 190x0) text (firma_rek/banka1) 450x24
      at (p1 + 0x30) t_nev "Bank2:" 160x24 at (p1 + 160x30) check (banka2 = "BOTP") 24x24
      [
        either face/data = true [ banka2: copy "BOTP" ][ banka2: copy "" ]
      ]
      at (p1 + 190x30) text (firma_rek/banka2) 450x24
    ]
    ;
    p1: p1 + 0x60
    append tabla_blk compose
    [
      at (p1) tlab center "Dodatni trošak" 501x26 color_h1 color_h2
      at (p1 + 500x0) tlab "Cena" 101x26 color_h1 color_h2
    ]
    p1: p1 + 0x25
    either prikaz_tr
    [
      mysql_cmd
      [
        "SELECT * FROM kalk_dokumenta WHERE br_dok='" rek/sif_dok "' AND tip='TR' ORDER BY rbr ASC "
      ]
      tar1: get_rekordset copy db
      foreach row1 tar1
      [
        append tabla_blk compose/only
        [ 
          at (p1) 
          tlab center (row1/rbr) 51x26 color_b1 color_b2
          with (compose/only [ data: (row1) ])
          [
            if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
            row1: face/data
            ablak_troska row1
          ]
          tlab (row1/naziv) 451x26 color_t1 color_t2
          tlab right (dec_form row1/jed_cena 0 2) 101x26 color_t1 color_t2
        ]
        p1: p1 + 0x25
      ]
      append tabla_blk compose/only
      [ 
        at (p1) 
        tlab center "Ins" 51x26 color_b1 color_b2
        [
          if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
          if (not string? rek/sif_dok) or (rek/sif_dok = "") [ alert "Fali otpremnica!" exit ]
          novi_red_troska
        ]
      ]
    ]
    [
      for i1 0 19 1
      [
        b1: copy "dod_tr" if i1 > 0 [ append b1 to-string i1 ]
        append tabla_blk compose [ at (p1 + 50x0) field (get in rek to-word b1) 451x26 ]
        b1: copy "dod_c"  if i1 > 0 [ append b1 to-string i1 ]
        append tabla_blk compose [ at (p1 + 500x0) field (get in rek to-word b1) 101x26 ]
        p1: p1 + 0x25
      ]
    ]
    if (int_value rek/col_fakt) > 1
    [
      blk2: make block! []
      blk2: parse/all rek/zbir_fakt "|"
      p1: p1 + 0x35
      append tabla_blk compose
      [
        at (p1) t_nev left bold "Collective invoice:" 200x24
        at (p1 +   0x30) text right "Dodati fakturu:" 130x24
        at (p1 + 130x30) arrow right 24x24 color_l2
        [
          rek/zbir_fakt: copy ""
          tabla_keres "faktura_col_inv"
          tabla/fejlec: "Izbor ino.fakture"
          tabla/name:   "faktura2 AS t1"
          tabla/lista:  rejoin
          [
            "t1.br_fak|t1.date|t1.sif_dok|t1.payment|t1.user|"
            "IFNULL((SELECT t2.name1 FROM " firma/data ".partners AS t2 "
                    "WHERE t2.sifra=t1.user LIMIT 1)," apjel apjel ") AS naziv_part|"
            "t1.delivery|t1.pakovanje|t1.opis|t1.neto|t1.bruto"
          ]
          tabla/nevek:  rejoin
          [ 
            "Broj fakture|Datum|Broj dokum.|Payment|Kupac|Naziv kupca|Delivery|Pakovanje|Opis|Neto|Bruto"
          ]
          tabla/oszlop: [ 100  100   90  100   70  120   100  100  100     90        90     ]
          tabla/tipus:  [ "LS" "CD" "CS" "CS" "CS" "LS"  "LS" "LS" "LS" "RFE:0.2" "RFE:0.2" ]
          tabla/order:  [ 1 "ASC" ]
          tabla/index:  [ 1 ]
          tabla/filter: rejoin [ "t1.br_fak LIKE '" rek/br_fak "%'" ]
          ;
          if string? tabla_korpe/fejlec
          [
            clear tabla_korpe/data
          ]
          otvaranje_korpe
          ;
          izbor_sifre [ 'btn "Izaberi" ]
          ;
          uk_neto:  0.0
          uk_bruto: 0.0
          if block? retval
          [
            alert "Koristi check-box!" exit
          ]
          if string? retval
          [
            if ((length? tabla_korpe/data) / 2) < 2
            [
              alert "Izaberi min.2 faktura!!!"
              rek/zbir_fakt: copy ""
              mysql_cmd
              [
                "UPDATE faktura2 SET zbir_fakt='" rek/zbir_fakt "',kor_modif='" rek/kor_modif "',dat_modif='" rek/dat_modif "' "
                "WHERE br_fak='" faktura_sif "' "
              ]
              mod_faktura
              exit
            ]
            if tabla_korpe/data/1 <> faktura_sif
            [
              alert rejoin [ "Izaberi faktura: " faktura_sif "!!!" ]
              rek/zbir_fakt: copy ""
              mysql_cmd
              [
                "UPDATE faktura2 SET zbir_fakt='" rek/zbir_fakt "',kor_modif='" rek/kor_modif "',dat_modif='" rek/dat_modif "' "
                "WHERE br_fak='" faktura_sif "' "
              ]
              mod_faktura
              exit
            ]
            poz1: head tabla_korpe/data
            while [ not tail? poz1 ]
            [
              if rek/zbir_fakt <> "" [ append rek/zbir_fakt ", " ]
              append rek/zbir_fakt poz1/1
              uk_neto:  uk_neto  + (dec_value (get_table_value "t1.neto"  poz1/2))
              uk_bruto: uk_bruto + (dec_value (get_table_value "t1.bruto" poz1/2))
              poz1: next next poz1
            ]
            append rek/zbir_fakt (rejoin [ "|" uk_neto "|" uk_bruto "|" "|" ])
            mod_faktura
          ]
          tabla/pane: none
          tabla_keres "faktura2_izb"
        ]
        at (p1 + 160x30)  info wrap (blk2/1) 440x56 color_g2
        at (p1 +   0x90)  t_nev "Neto:"  160x24
        at (p1 + 160x90)  t_fld blk2/2
        at (p1 +   0x120) t_nev "Bruto:" 160x24
        at (p1 + 160x120) t_fld blk2/3
        at (p1 +   0x150) t_nev "Pakovanje:" 160x24
        at (p1 + 160x150) t_fld blk2/4 440x24
        at (p1 +   0x180) text right "Štampati collective invoice:" 200x24
        at (p1 + 200x180) btn "Štamp.Col.invoice" 150x24 color_l2
        [
          if empty? blk2 [ alert "Fakture nisu izabrane!" exit ]
          if blk2/4 = "" [ alert "Fali pakovanje!" exit ]
          rek/zbir_fakt: blokk_pakolo blk2 "|"
          blk3: make block! []
          blk3: parse blk2/1 ", "
          if (length? blk3) < 2
          [
            alert "Izaberi min. 2 faktura!"
            rek/zbir_fakt: copy ""
            mod_faktura
            exit
          ]
          if rek/faza < "G" 
          [
            either not none? banka1 
            [ 
              rek/lista_banke: copy banka1
              if not none? banka2 [ append rek/lista_banke rejoin [ "|" banka2 ] ] 
            ]
            [
              if not none? banka2 [ rek/lista_banke: copy banka2 ]
            ] 
            if (rek/lista_banke = "") or (rek/lista_banke = "|") [ alert "Fali banke!" exit ]
          ]
          mysql_cmd
          [
            "UPDATE faktura2 SET zbir_fakt='" rek/zbir_fakt "',kor_modif='" rek/kor_modif "',dat_modif='" rek/dat_modif "' "
            "WHERE br_fak='" faktura_sif "' "
          ]
          stamp_invoice blk3 blk2
        ]
      ]
      p1: p1 + 0x180 ; panel start
    ]
    p1: p1 + 60x40 ; panel start
    blk_panel: uplate_panel_rajzolo p1 god0
    p2: blk_panel/2
    append tabla_blk compose
    [
      at (p1) panel blk_panel/1 with [ edge: [color: coal size: 1x1 ]]
    ]
    ;
    p1: p1 + (as-pair 0 (p2/2 + 10)) ; panel start
    blk_panel2: ulfakt_panel_rajzolo p1
    p2: blk_panel2/2
    append tabla_blk compose
    [
      at (p1) panel blk_panel2/1 with [ edge: [color: coal size: 1x1 ]]
    ]
    p1: max (p1 + p2) ablak_c/size
    ablak_c/pane: layout/offset tabla_blk ablak_c/user-data
    show ablak_c
  ]

  ablak_brajz: func []
  [
    ; tabla - kereso beallitasai:
    tabla_keres "faktura2_izb"
    tabla/akcio: func [ adat ]
    [
      tabla_keres "faktura2_izb"
      faktura_sif: copy adat/1
      tabla/value:  reduce [ faktura_sif  ]
      tabla_view
      mysql_cmd 
      [ 
        "SELECT t1.*,"
        "(SELECT t2.rok_pl FROM " firma/data ".partners AS t2 WHERE t2.sifra=t1.user) AS rok_pl,"
        "(SELECT COUNT(*) FROM faktura2 AS t3 WHERE t3.br_fak LIKE CONCAT(t1.br_fak,'%')) AS col_fakt "
        "FROM faktura2 AS t1 "
        "WHERE t1.br_fak='" faktura_sif "' " 
      ]
      rek: get_rekord first db
      mode1/lista_fak: make block! []
      either retval [ mod_faktura ][ alert "Faktura nije u bazi!" faktura_sif: copy "" ]
    ]
    tabla/pane: make block! compose
    [
      at 450x20 btn "Fali dat.istupa" 120x30 color_b4
      [
        set_table_form compose [ clr "uslov" uslov "t1.date" (rejoin [ "<:" sistime/isodate ]) uslov "t1.dat_istupa" (rejoin [ "=:" zero_datum ]) ]
        ablak_brajz
      ]
      at 580x20 btn "Brisati svako filtriranje" 180x30 red
      [
        set_table_form [ clr "uslov" ]
        ablak_brajz
      ]
    ]
    tabla/value:  reduce [ faktura_sif ]
    tabla/ablak:  ablak_b
    tabla_view
  ]
  
  ablak_arajz: func []
  [
    meretb: meretx - wmeret/m - 40
    ablak_a/pane: layout/offset compose
    [
      size ablak_a/size
      backtile polished color_g2
      across
      at 10x10 arrow 24x24 left color_m2
      [ program_nazad ]
      at 0x40 ablak_b: box color_g4 (as-pair meretb (merety - 60)) with [ edge: [color: coal size: 1x1 ] ]
      at (as-pair 0 (merety - 20)) slider (as-pair meretb 20) with [ user-data: ablak_b ]
      [ scroller_pozicio face ]
      at (as-pair meretb 40) slider (as-pair 20 (merety - 40)) with [ user-data: ablak_b ]
      [ scroller_pozicio face ]
      at (as-pair (meretb + 20) 40) ablak_c: box color_g4 (as-pair (meretx - meretb - 40) (merety - 40)) with [ edge: [color: coal size: 1x1 ] ]
      at (as-pair (meretx - 20) 40) slider (as-pair 20 (merety - 40)) with [ user-data: ablak_c ]
      [ scroller_pozicio face ]
    ] 0x0
    ablak_b/user-data: 0x0
    ablak_c/user-data: 0x0
    main_window/user-data: ablak_c ; gorgo bekapcsolasa
    show ablak_a
  ]

  set 'faktura_izb: func [ ]
  [
    prikaz_tr: true
    faktura_sif: copy ""
    paym_dat: sistime/isodate
    ;
    mode: make object!
    [
      dat_od_ios:  sistime/isodate 
      dat_do_ios:  sistime/isodate
      dat_izv:     rejoin [ baze_sif "-12-31" ]
      vrsta_datum: copy ""
      partner:     copy ""
    ]
    ;
    mode1: make object!
    [
      br_fak:    copy ""
      dat_fak:   copy ""
      lista_fak: make block! []
    ]
    ;
    ablak_arajz
    ablak_cezar
    main_window/user-data: ablak_c ; gorgo-objektum
    if not empty? nyomelem
    [
      faktura_sif: copy nyomelem/1
      remove nyomelem
      mysql_cmd 
      [ 
        "SELECT t1.*,"
        "(SELECT t2.rok_pl FROM " firma/data ".partners AS t2 WHERE t2.sifra=t1.user) AS rok_pl,"
        "(SELECT COUNT(*) FROM faktura2 AS t3 WHERE t3.br_fak LIKE CONCAT(t1.br_fak,'%')) AS col_fakt "
        "FROM faktura2 AS t1 "
        "WHERE t1.br_fak='" faktura_sif "' " 
      ]
      rek: get_rekord first db
      either retval [ mod_faktura ][ alert "Faktura nije u bazi !"  ]
    ]
    ablak_brajz
  ]
]

append cleanup
[
  ino_fakt_def_01
  faktura_izb
  rek
]
