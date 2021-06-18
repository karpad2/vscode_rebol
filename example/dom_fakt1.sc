;
; Fakturisanje:
;
; globalis
;  rek - rekord dom-fakture , rekord univerzalne fakture

faktura_def_01: context
[
  faktura_sif: none
  mode:        none
  sif_part:    copy "" ; a partner kivalasztasahoz, az isvestajoknal
  naz_part:    copy "" ; a partner kivalasztasahoz, az isvestajoknal
  ;
  xml_rekap_ir_detaljno: func [ mode[object!] /local tar0 sif0 row0 tar1 row1 poz0 k0 uk_iznosp uk_iznos ukizn_val val0 sz1 sz2 sz3 i0 n0 oour0 ]
  [
    tar1: make block! []
    uk_iznos:  0    ; ukupno iznos
    uk_iznosp: 0    ; ukupno iznos po partnerima
    uk_uplata: 0    ; ukupno uplata
    uk_saldo:  0    ; ukupno saldo
    ukizn_val: 0    ; uk.iznos po valute
    i0: 0
    n0: 0
    oour0: copy ""   ; 
    sz1:   copy ""
    sz2:   copy ""
    sz3:   copy ""
    ;
    if mode/partner <> ""
    [
      sz1: rejoin [ "AND t1.sif_part=" apjel (mode/partner) apjel " " ]
    ]
    mysql_cmd compose
    [
      "SELECT t1.br_rac,t1.sif_part,t1.ukupno,t1.datum,MID(t1.datum,1,4) AS godina,t1.br_dok,"
      "'' AS sif_oour,t2.name1 AS partner,t2.sif_drzave,"
      "(SELECT t4.otp_valuta FROM " baze_pret ".dokument AS t4 WHERE t4.br_dok=t1.br_dok) AS valuta,"
      "(SELECT t4.sif_mag FROM " baze_pret ".dokument AS t4 WHERE t4.br_dok=t1.br_dok) AS sif_mag "
      "FROM " baze_pret ".faktura AS t1 "
      "LEFT JOIN " firma/data ".partners AS t2 ON t2.sifra=t1.sif_part "
      "WHERE (t1.datum BETWEEN '" mode/dat_od_ios "' AND '" mode/dat_do_ios "') " (sz1)
      "ORDER BY valuta ASC,partner ASC,t1.br_rac ASC "
    ]
    tar0: get_rekordset copy db
    mysql_cmd compose
    [
      "SELECT t1.br_rac,t1.sif_part,t1.ukupno,t1.datum,MID(t1.datum,1,4) AS godina,t1.br_dok,"
      "'' AS sif_oour,t2.name1 AS partner,t2.sif_drzave,"
      "(SELECT t4.otp_valuta FROM dokument AS t4 WHERE t4.br_dok=t1.br_dok) AS valuta,"
      "(SELECT t4.sif_mag FROM dokument AS t4 WHERE t4.br_dok=t1.br_dok) AS sif_mag "
      "FROM faktura AS t1 "
      "LEFT JOIN " firma/data ".partners AS t2 ON t2.sifra=t1.sif_part "
      "WHERE (t1.datum BETWEEN '" mode/dat_od_ios "' AND '" mode/dat_do_ios "') " (sz1)
      "ORDER BY valuta ASC,partner ASC,t1.br_rac ASC "
    ]
    append tar0 get_rekordset copy db
    ;
    xml_blk: create_excel_doc
    xml_rek/fname: rejoin [ dir_eximp "Rekapitulacija_izl_rac_" sistime/isodate "_det." firma_rek/xml_ext ]
    xml_rek/fmode: true
    xml_rek/auto_size: true

    excel_com compose
    [
      sheet "ios_izvoz"
      column 40 180 45 40 80 80 80 50 70
      format "f:0" align "h:Center" "v:Center" "w:1" ; fejlec
      interior "c:#E6E6E6" "p:Solid"
      ;
      row  "h:30" interior 0 align "h:Center" font "b:1"
      cell ("Šif.^/Part.")
      cell ("Partner")
      cell ("Lokacija")
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
    poz0: head tar0
    while [ not tail? poz0 ]
    [
      row0: poz0/1
      ;-- koszovo dinarban
      if row0/sif_drzave = "XK"
      [
        mysql_cmd
        [
          "SELECT t1.br_fak,t1.iznos,t1.izn_pdv FROM dok_fakt AS t1 "
          "WHERE t1.br_fak='" row0/br_rac "'"
        ]
        rek0: get_rekord first db
        row0/ukupno: (dec_value rek0/iznos) + (dec_value rek0/izn_pdv)
        row0/valuta: copy "RSD"
      ]
      if row0/br_dok/1 = #"7" [ row0/valuta: copy "RSD" ]
      row1: select magacin_blk to-integer row0/sif_mag
      if not none? row1
      [
        row0/sif_oour: copy row1/lok_oour
      ]
      append tar1 (rejoin [ row0/sif_oour "|" row0/valuta "|" row0/partner "|" row0/br_rac ])
      append tar1 row0
      poz0: next poz0
    ]
    sort/skip tar1 2
    n0: length? tar1
    n0: n0 / 2
    foreach [ sif0 row0 ] tar1
    [
      i0: i0 + 1
      if oour0 = "" [ oour0: copy row0/sif_oour ]
      ;
      if oour0 <> row0/sif_oour
      [
        sz1: rejoin [ "Ukupno po lokacija: " (oour0) ":" ]
        excel_com compose
        [
          row interior 0 font "b:1"
          format "f:@" align "h:Right"
          cell merge 5 (sz1)
          format "f:###,##0.00" align "h:Right"
          cell (ukizn_val)
        ]
        ukizn_val: 0
      ]
      
      uk_iznosp: round/to (dec_value row0/ukupno) 0.01
      ukizn_val: ukizn_val + (dec_value row0/ukupno)
      ukizn_val: round/to ukizn_val 0.01
      uk_iznos:  uk_iznos  + (dec_value row0/ukupno)
      uk_iznos:  round/to uk_iznos 0.01
      excel_com compose
      [
        row interior 0 font "b:0"
        ;border "p:Top" "l:Continuous" "w:0.5" "p:Bottom" "l:Continuous" "w:0.5" "p:Left" "l:Continuous" "w:0.5" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
        format "f:@" align "h:Left"
        cell (row0/sif_part)
        cell (row0/partner)
        format "f:@" align "h:Center"
        cell (row0/sif_oour)
        cell (row0/valuta)
        cell (row0/br_rac)
        cell (row0/datum)
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
        sz1: rejoin [ "Ukupno po lokacija: " (row0/sif_oour) ":" ]
        excel_com compose
        [
          row interior 0 font "b:1"
          ;border "p:Top" "p:Bottom" "p:Left" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
          format "f:@" align "h:Right"
          cell merge 5 (sz1)
          format "f:###,##0.00" align "h:Right"
          cell (ukizn_val)
        ]
        ukizn_val: 0
      ]
      ;
      oour0: copy row0/sif_oour
    ]
    ;-- meg nem hasznalom
    if sif_part = ""
    [
      excel_com compose
      [
        row interior 0 font "b:1"
        ;border "p:Top" "p:Bottom" "p:Left" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
        format "f:@" align "h:Right"
        cell merge 5 "Ukupno:"
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
  
  xml_rekap_ir: func [ mode[object!] /local poz0 poz1 tar0 row0 tar1 row1 k0 uk_iznosp uk_iznos ukizn_val val0 sz1 sz2 sz3 i0 n0 blk_part oour0 blk0 ]
  [
    tar1: make block! []
    blk_part: make block! []
    uk_iznos:  0    ; ukupno iznos
    uk_iznosp: 0    ; ukupno iznos po partnerima
    uk_uplata: 0    ; ukupno uplata
    uk_saldo:  0    ; ukupno saldo
    ukizn_val: 0    ; uk.iznos po valute
    i0: 0
    n0: 0
    oour0: copy ""   ; 
    sz1:   copy ""
    sz2:   copy ""
    sz3:   copy ""
    ;
    if mode/partner <> ""
    [
      sz1: rejoin [ "AND t1.sif_part=" apjel (mode/partner) apjel " " ]
    ]
    mysql_cmd compose
    [
      "SELECT t1.br_rac,t1.sif_part,t1.ukupno,t1.datum,MID(t1.datum,1,4) AS godina,t1.br_dok,"
      "'' AS sif_oour,t2.name1 AS partner,t2.sif_drzave,"
      "(SELECT name1 FROM " firma/data ".partners AS t2 WHERE t2.sifra=t1.sif_part) AS partner,"
      "(SELECT t4.otp_valuta FROM " baze_pret ".dokument AS t4 WHERE t4.br_dok=t1.br_dok) AS valuta,"
      "(SELECT t4.sif_mag FROM " baze_pret ".dokument AS t4 WHERE t4.br_dok=t1.br_dok) AS sif_mag "
      "FROM " baze_pret ".faktura AS t1 "
      "LEFT JOIN " firma/data ".partners AS t2 ON t2.sifra=t1.sif_part "
      "WHERE (t1.datum BETWEEN '" mode/dat_od_ios "' AND '" mode/dat_do_ios "') " (sz1)
    ]
    tar0: get_rekordset copy db
    mysql_cmd compose
    [
      "SELECT t1.br_rac,t1.sif_part,t1.ukupno,t1.datum,MID(t1.datum,1,4) AS godina,t1.br_dok,"
      "'' AS sif_oour,t2.name1 AS partner,t2.sif_drzave,"
      "(SELECT name1 FROM " firma/data ".partners AS t2 WHERE t2.sifra=t1.sif_part) AS partner,"
      "(SELECT t4.otp_valuta FROM dokument AS t4 WHERE t4.br_dok=t1.br_dok) AS valuta,"
      "(SELECT t4.sif_mag FROM dokument AS t4 WHERE t4.br_dok=t1.br_dok) AS sif_mag "
      "FROM faktura AS t1 "
      "LEFT JOIN " firma/data ".partners AS t2 ON t2.sifra=t1.sif_part "
      "WHERE (t1.datum BETWEEN '" mode/dat_od_ios "' AND '" mode/dat_do_ios "') " (sz1)
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
      column 40 180 45 40 80 50 70
      format "f:0" align "h:Center" "v:Center" "w:1" ; fejlec
      interior "c:#E6E6E6" "p:Solid"
      ;
      row  "h:30" interior 0 align "h:Center" font "b:1"
      cell ("Šif.^/Part.")
      cell ("Partner")
      cell ("Lokacija")
      cell ("Valuta")
      cell ("Iznos")
    ]
    if sif_part <> ""
    [
      excel_com compose
      [
        cell ("Br.fakt.")
      ]
    ]
    ;
    poz0: head tar0
    while [ not tail? poz0 ]
    [
      row0: poz0/1
      ;-- koszovo dinarban
      if row0/sif_drzave = "XK"
      [
        mysql_cmd
        [
          "SELECT t1.br_fak,t1.iznos,t1.izn_pdv FROM dok_fakt AS t1 "
          "WHERE t1.br_fak='" row0/br_rac "'"
        ]
        rek0: get_rekord first db
        row0/ukupno: (dec_value rek0/iznos) + (dec_value rek0/izn_pdv)
        row0/valuta: copy "RSD"
      ]
      if row0/br_dok/1 = #"7" [ row0/valuta: copy "RSD" ]
      row1: select magacin_blk to-integer row0/sif_mag
      if not none? row1
      [
        row0/sif_oour: copy row1/lok_oour
      ]
      ;append tar1 (rejoin [ row0/sif_oour "|" row0/valuta "|" row0/partner ])
      ;append tar1 row0
      poz0: next poz0
    ]
    poz0: head tar0
    while [ not tail? poz0 ]
    [
      row0: poz0/1
      append blk_part (rejoin [ row0/sif_oour "|" row0/valuta "|" row0/partner "|" row0/sif_part "|" row0/valuta ])
      poz0: next poz0
    ]
    blk_part: unique blk_part
    poz0: head blk_part
    while [ not tail? poz0 ]
    [
      insert at blk_part ((index? poz0) + 1) 0.0
      poz0: next next poz0
    ]
    sort/skip blk_part 2
    poz0: head tar0
    while [ not tail? poz0 ]
    [
      row0: poz0/1
      ;
      sz1: rejoin [ row0/sif_oour "|" row0/valuta "|" row0/partner "|" row0/sif_part "|" row0/valuta ]
      poz1: index_block_find blk_part sz1 2
      if poz1/1 = sz1
      [
        poz1/2: (dec_value poz1/2) + (dec_value row0/ukupno)
      ]
      ;
      poz0: next poz0
    ]
    ;
    n0: length? blk_part
    n0: n0 / 2
    ;
    poz0: head blk_part
    while [ not tail? poz0 ]
    [
      i0: i0 + 1
      blk0: parse/all poz0/1 "|"
      if oour0 = "" [ oour0: copy blk0/1 ]
      if oour0 <> blk0/1
      [
        sz1: rejoin [ "Ukupno po lokacija: " (oour0) ":" ]
        excel_com compose
        [
          row interior 0 font "b:1"
          format "f:@" align "h:Right"
          cell merge 3 (sz1)
          format "f:###,##0.00" align "h:Right"
          cell (ukizn_val)
        ]
        ukizn_val: 0
      ]
      ;
      uk_iznosp: round/to (dec_value poz0/2) 0.01
      ukizn_val: ukizn_val + (dec_value poz0/2)
      ukizn_val: round/to ukizn_val 0.01
      uk_iznos:  uk_iznos  + (dec_value poz0/2)
      uk_iznos:  round/to uk_iznos 0.01
      ;
      excel_com compose
      [
        row interior 0 font "b:0"
        ;border "p:Top" "l:Continuous" "w:0.5" "p:Bottom" "l:Continuous" "w:0.5" "p:Left" "l:Continuous" "w:0.5" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
        format "f:@" align "h:Left"
        cell (blk0/4)
        cell (blk0/3)
        format "f:@" align "h:Center"
        cell (blk0/1)
        cell (blk0/2)
        format "f:###,##0.00" align "h:Right"
        cell (poz0/2)
      ]
      oour0: copy blk0/1
      poz0: next next poz0
    ]
    sz1: rejoin [ "Ukupno po lokacija: " (oour0) ":" ]
    excel_com compose
    [
      row interior 0 font "b:1"
      ;border "p:Top" "p:Bottom" "p:Left" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
      format "f:@" align "h:Right"
      cell merge 3 (sz1)
      format "f:###,##0.00" align "h:Right"
      cell (ukizn_val)
    ]
    ukizn_val: 0
    excel_com compose
    [
      row interior 0 font "b:1"
      ;border "p:Top" "p:Bottom" "p:Left" "p:Right" "l:Continuous" "w:0.5" "c:#000000" ; keretezve alul-felul
      format "f:@" align "h:Right"
      cell merge 3 "Ukupno:"
      format "f:###,##0.00" align "h:Right"
      cell (uk_iznos)
    ]
    
    excel_com
    [
      autosize
      endsheet
    ]
    write_xml_doc xml_blk
  ]
  
  ios_adatok_olvas: func [ mode[object!] /local tar0 row0 tar1 sz0 sz1 sz2 dat1 ]
  [
    dat1: sistime/isodate
    tar1: make block! []
    sz0: copy ""
    sz1: copy ""
    sz2: copy ""
    ;
    if mode/vrsta_datum = "" [ alert "Izaberi vrsta datuma!" return tar1 ]
    if mode/dat_do_ios < dat1 [ dat1: mode/dat_do_ios ]
    if mode/vrsta_datum = "datum fakt."
    [ 
      append sz0 rejoin [ "AND t1.datum<=" apjel (mode/dat_do_ios) apjel " " ]
      append sz1 rejoin [ "AND t2.datum<=" apjel (mode/dat_do_ios) apjel " " ]
      append sz2 rejoin [ "AND t3.datum<=" apjel (dat1) apjel " " ]
    ]
    if mode/vrsta_datum = "datum valuta."
    [
      append sz0 rejoin [ "AND (t1.dat_val BETWEEN " apjel (mode/dat_od_ios) apjel " AND " apjel (mode/dat_do_ios) apjel ") " ]
      append sz1 rejoin [ "AND t2.datum<=" apjel (mode/dat_do_ios) apjel " " ]
      append sz2 rejoin [ "AND t3.datum<=" apjel (dat1) apjel " " ]
    ]
    if mode/vrsta_datum = "open_invoice"
    [
      append sz0 rejoin [ "AND t1.datum>=" apjel  "2020-01-01"  apjel " AND t1.datum<=" apjel (mode/dat_izv) apjel " " ]
      append sz1 rejoin [ "AND t2.datum<=" apjel (mode/dat_izv) apjel " " ]
      append sz2 rejoin [ "AND t3.datum<=" apjel (mode/dat_izv) apjel " " ]
    ]
    if mode/partner <> "" [ append sz0 rejoin [ "AND t1.sif_part=" apjel (mode/partner) apjel " "  ] ]
    ;
    mysql_cmd compose
    [
      "SELECT CONCAT('RSD','|',t1.naz_part,'|',t1.dat_val,'|',t1.br_fak) AS kulcs,t1.sif_part AS user,t1.br_fak,(t1.iznos + t1.izn_pdv) AS uk_izn,"
      "t1.datum AS date,t1.dat_val,t1.naz_part AS partner,'RSD' AS valuta,"
      "IFNULL((SELECT SUM(t2.uplata) FROM upl_fakt AS t2 "
              "WHERE t2.br_fak=t1.br_fak AND t2.sif_part=t1.sif_part " (sz1) " AND t2.vrsta<>'U' AND t2.vrsta<>'P'),0) AS uplata "
      "FROM dok_fakt AS t1 "
      "WHERE t1.vrsta='DOM' AND t1.tip='R' AND t1.tip_dok='R' AND t1.godina BETWEEN '2018' AND '" ((int_value baze_sif) - 1) "' "
      (sz0) " AND t1.sif_part<>'253' "
      "UNION ALL "
      "SELECT CONCAT('RSD','|',t2.name1,'|',t1.dat_val,'|',t1.br_rac) AS kulcs,t1.sif_part AS user,t1.br_rac AS br_fak,t1.ukupno AS uk_izn,"
      "t1.datum AS date,t1.dat_val,t2.name1 AS partner,'RSD' AS valuta,"
      "(IFNULL((SELECT SUM(t3.uplata) FROM upl_fakt AS t3 "
               "WHERE t3.br_fak=t1.br_rac AND t3.sif_part=t1.sif_part " (sz2) " AND t3.vrsta<>'U' AND t3.vrsta<>'P'),0)) AS uplata "
      "FROM faktura AS t1 "
      "LEFT JOIN " firma/data ".partners AS t2 ON t2.sifra=t1.sif_part "
      "WHERE 1 " (sz0) " AND t1.sif_part<>'253' "
      "ORDER BY valuta ASC,partner ASC,br_fak ASC"
    ]
    tar0: get_rekordset copy db
    ;
    either mode/vrsta_datum = "open_invoice"
    [
      foreach row0 tar0
      [
        append tar1 row0/kulcs
        append tar1 row0
      ]
    ]
    [
      foreach row0 tar0
      [
        k1: round/to (dec_value row0/uk_izn) 0.01
        k2: round/to (dec_value row0/uplata) 0.01
        if (k1 - k2) > 0
        [
          append tar1 row0/kulcs
          append tar1 row0
        ]
      ]
    ]
    sort/skip tar1 2  ;-- sorba rakom
    return tar1
  ]
  
  ios_pdf: func [ mode[object!] /local tar0 sif0 row0 k0 uk_iznos uk_uplata uk_saldo old fp1 dok_iso y1 f1 sz1 sz2 sz3 sif_part0 
                                       ukpart_izn ukpart_upl ukpart_sld akt_dat ]
  [
    k0:         0    ; saldo
    uk_iznos:   0    ; ukupno iznos
    uk_uplata:  0    ; ukupno uplata
    uk_saldo:   0    ; ukupno saldo
    ukpart_izn: 0
    ukpart_upl: 0
    ukpart_sld: 0
    sz1: copy ""
    sz2: copy ""
    sz3: copy ""
    akt_dat:  sistime/isodate
    ;
    fp1: flash "Obrada podataka..."
    tar0: ios_adatok_olvas mode
    if (length? tar0) = 0 [ alert "Nema podataka!" unview/only fp1 exit ]
    ;
    load_pdf_font "A1"
    load_pdf_font "AB"
    load_pdf_font "AIB"
    pdf_blk: create_pdf 598x842 ; portrait A4-es
    pdf_rek/fname: join dir_lokal rejoin [ "IOS_do_" mode/dat_do_ios ".pdf" ]
    pdf_rek/fmode: true
    pdf_rek/poz1: as-pair 20 (pdf_rek/pagesz/2 - 50) ; bal felso szele
    pdf_rek/poz2: as-pair (pdf_rek/pagesz/1 - 50) 20 ; jobb also szele
    pdf_rek/poz: 0x0 ; a lap aljan kezdi hogy uj lap kovetkezzen
    old: 0 ; oldal szamlalo mindig nullazni itt
    dok_iso: copy "___.___"  ; formular szama ha kell
    either firma_rek/logo_izbor1 = "2"
    [
      new_pdf_image pdf_blk to-string firma_rek/stamp_logo2     ; alapertelmezett fejlec ez az ujabb
      fejlec:
      [
        fn "A1" 12 cg 1.0 lw 0.25 tv middle ct 0.0 cb 1.0
        nl 0x0 ta left im win 135x19 0x0 
        nl 0x20 fn "AB" 12
        ta center "IOS Domaæi tr." 560x24
        fn "A1" 11 ta right tb -50 (rejoin [ dok_iso "^/Strana: " old ]) 50x24
        nl 0x20 ta center (rejoin [ "Do: " mode/dat_do_ios ]) 560x16
      ]
    ]
    [
      new_pdf_image pdf_blk to-string firma_rek/stamp_logo
      fejlec:
      [
        fn "A1" 12 cg 1.0 lw 0.25 tv middle ct 0.0 cb 1.0
        nl 0x40 ta left im win 56x40 15x0 fn "AB" 12
        ta center "IOS Domaæi tr." 470x24
        fn "A1" 11 ta right (rejoin [ dok_iso "^/Strana: " old ]) 80x24
        nl 0x20 ta center (rejoin [ "Do: " mode/dat_do_ios ]) 470x16
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
    foreach [ sif0 row0 ] tar0
    [
      k0: (dec_value row0/uk_izn) - (dec_value row0/uplata)
      k0: round/to k0 0.01
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
      uk_iznos:  (dec_value uk_iznos ) + (dec_value row0/uk_izn)
      uk_uplata: (dec_value uk_uplata) + (dec_value row0/uplata)
      uk_saldo:  (dec_value uk_saldo ) + (dec_value k0)
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
        nl (as-pair 0 y1) fn "A1" 8 cg 1.0 lw 0.25 ct 0.0 cb 0.5 tv middle ta center
        (row0/user) (as-pair 30 y1) 
        ta left
        (row0/partner) (as-pair 150 y1)
        ta center
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
    pdfcom compose
    [
      nl 0x12 fn "AB" 8 cg 1.0 lw 0.25 ct 0.0 cb 1.0 tv middle ta right
      "UKUPNO:" 350x12
      cb 0.5
      (money_form uk_iznos  2) 70x12
      (money_form uk_uplata 2) 70x12
      (money_form uk_saldo  2) 70x12
    ]
    unview/only fp1
    write_pdf_file pdf_blk
  ]
  
  ios_xml: func [ mode[object!] /local tar0 row0 k0 fp1 ]
  [
    k0:  0    ; saldo
    ;
    xml_blk: create_excel_doc
    xml_rek/fname: rejoin [ dir_eximp "IOS_do_" mode/dat_do_ios "." firma_rek/xml_ext ]
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
    if (length? tar0) = 0 [ alert "Nema podataka!" unview/only fp1 exit ]
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
  
  ablak_cezar: func [ /local t_sif row1 sz1 dat_od_ios dat_do_ios ]
  [
    novi_sif: copy ""
    dat_od_ios: sistime/isodate 
    dat_do_ios: sistime/isodate
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
      at 10x50 text right "Nova šifra:" 100x24
      at 110x50 t_sif: field  120x24
      at 250x50 button "Upis" 80x24 color_m2
      [
        sz1: copy t_sif/text
        if sz1 = ""
        [
          if none? row1: oblik_dokum_rekord "FAK" "R" [ alert "Tip fakture nije dobar!" exit ]
          sz1: dokumentum_szam_keszito row1/dok_form1 baze_sif "faktura" "br_rac"
          ;
          if not string? sz1 [ exit ]
        ]
        if (length? sz1) < 4 [ alert "Broj dokumenta nije odreðen!" exit ]
        mysql_cmd [ "SELECT br_rac FROM faktura WHERE br_rac = '" sz1 "'" ]
        if (not empty? first db)
        [
          alert rejoin [ ~"Faktura:" " '" sz1 "' " ~"veæ postoji!" ] exit
        ]
        ; beirni az uj rekordot:
        faktura_sif: copy sz1
        mysql_cmd
        [
          "INSERT INTO faktura (br_rac,datum,rok,dat_val,namena,kor_insert,dat_insert ) "
          "VALUES ('" faktura_sif "','" sistime/isodate "','" firma_rek/rok_fakt
          "','" to-iso-date ((sistime/date) + firma_rek/rok_fakt) "','virmanom','" korisnik "','" sistime/isodate "') "
        ]
        ablak_brajz
        mysql_cmd [ "SELECT * FROM faktura WHERE br_rac = '" faktura_sif "' " ]
        rek: get_rekord first db
        either retval [ mod_faktura ][ alert "faktura nije u bazi!" faktura_sif: copy "" ]
      ]
    ]
    if find [ "prg" "adm" "sis" ] dozvola
    [
      append tabla_blk compose
      [
        at 360x100 button "Gotovo sve" 120x24 color_m2
        [
          if not write_mode [ exit ]
          izbor_fraze "Upisati fazu G za:" [ "faza<'G'" "faza>'G'" ] none
          if not string? retval [ exit ]
          mysql_cmd compose [ "UPDATE faktura SET faza='G' WHERE " (retval) " " ]
          ablak_brajz
        ]
      ]
    ]
    append tabla_blk compose
    [
      at  5x200 box color_g4 550x330 with [ edge: [color: coal size: 1x1] ]
      at 10x205 text bold "Izveštaji:" 220x24
      at 10x240 text "Izvod Otvorenih Stavki:" 220x24
      at 250x240 btn    "IOS <PDF>" 100x24 color_l2
      [
        ios_pdf mode
      ]
      at 360x240 btn "IOS <XML>" 100x24 color_l6
      [
        ios_xml mode
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
        knjig_partner_izbor mode/partner none "W0"
        if object? retval
        [
          mode/partner:  copy retval/sifra
          mode/naz_part: copy retval/name1
          ablak_cezar
        ]
      ]
      at 140x330 info center (mode/partner) 70x24 color_g2
      at 220x330 text (mode/naz_part) 400x24
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
    ablak_c/pane: layout/offset tabla_blk 0x0
    t_sif/text: copy ""
    show t_sif
    show ablak_c
  ]
  
  novi_red_troska: func [/local row1 n1 ]
  [
    if not write_mode [ alert "Nije dozvoljeno!" exit ]
    mysql_cmd
    [
      "SELECT MAX(rbr) AS broj FROM kalk_dokumenta WHERE br_dok='" rek/br_dok "' "
    ]
    row1: get_rekord first db
    n1: int_value row1/broj
    n1: n1 + 1
    mysql_cmd
    [
      "INSERT IGNORE INTO kalk_dokumenta SET br_dok='" rek/br_dok "',tip='TR',rbr='" n1 "' "
    ]
    if not mysts [ exit ]
    mod_faktura
    mysql_cmd
    [
      "SELECT * FROM kalk_dokumenta WHERE br_dok='" rek/br_dok "' AND tip='TR' AND rbr='" n1 "' "
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
          "DELETE FROM kalk_dokumenta WHERE br_dok='" rek/br_dok "' AND mbr='" row1/mbr "' "
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
          "WHERE br_dok='" rek/br_dok "' AND mbr='" row1/mbr "' "
        ]
        mod_faktura
        hide-popup
      ]
    ]
    ablak_mp: layout/offset tabla_blk 0x0
    ;
    inform/title ablak_mp "Dodatni trošak"
  ]
  
  faktura_kiiras: func []
  [  
    rek/dat_modif: sistime/isodate
    rek/kor_modif: korisnik
    mysql_cmd 
    [ 
      "UPDATE faktura SET datum='" rek/datum "',rok='" rek/rok "',veza_dok='" rek/veza_dok "',"
      "dat_val='" rek/dat_val "',br_dok='" rek/br_dok "',datum_dok='" rek/datum_dok "',"
      "sif_mag='" rek/sif_mag "',sif_part='" rek/sif_part "',namena='" rek/namena "',"
      "otprema='" rek/otprema "',prevoz='" rek/prevoz "',rabat='" rek/rabat "',"
      "troskovi='" rek/troskovi "',opis='" rek/opis "',opis1='" rek/opis1 "',opis2='" rek/opis2 "',"
      "predracun='" rek/predracun "',dat_avans='" rek/dat_avans "',avans18='" rek/avans18 "',"
      "pdv18='" rek/pdv18 "',avans8='" rek/avans8 "',pdv8='" rek/pdv8 "',"
      "predracun_1='" rek/predracun_1 "',dat_avans_1='" rek/dat_avans_1 "',avans18_1='" rek/avans18_1 "',"
      "pdv18_1='" rek/pdv18_1 "',avans8_1='" rek/avans8_1 "',pdv8_1='" rek/pdv8_1 "',"
      "predracun_2='" rek/predracun_2 "',dat_avans_2='" rek/dat_avans_2 "',avans18_2='" rek/avans18_2 "',"
      "pdv18_2='" rek/pdv18_2 "',avans8_2='" rek/avans8_2 "',pdv8_2='" rek/pdv8_2 "',na1='" rek/na1 "',"
      "kor_modif='" rek/kor_modif "',dat_modif='" rek/dat_modif "' WHERE br_rac='" faktura_sif "'" 
    ]
    ; TODO: kiirni a dok_fakt tablaba is:
    if rek/sif_fak <> ""
    [
      mysql_cmd
      [
        "UPDATE dok_fakt SET datum='" rek/datum "',br_dok='" rek/br_dok "' WHERE br_fak='" rek/sif_fak "' "
      ]
    ]
  ]
  
  mod_faktura: func [ /local row1 row3 pnev blk blk1 blk_upl god0 uk_uplata saldo saldo0 
                      p1 tar1 tar2 row2 k1 ino_upl uk_upl plata_rok plata_dfak sz1 naziv2 ]
  [
    tar2: none
    god0: copy/part rek/datum 4
    uk_uplata: 0.0
    saldo: 0.0
    k1:    0.0
    ino_upl:    0.0
    uk_upl:     0.0
    plata_rok:  0.0
    plata_dfak: 0.0
    sz1: copy ""
    ; ha a naziv2-t kell hozni a hazai szamlara akkor a checkbox X
    either rek/na1 = "d" [ naziv2: true ][ naziv2: false ]
    if rek/sif_part <> ""
    [
      mysql_cmd
      [
        "SELECT name1 FROM " firma/data ".partners WHERE sifra='" rek/sif_part "' "
      ]
      row1: first db
      either empty? row1 [ pnev: "" ][ pnev: row1/1/1 ]
    ]
    tabla_blk: make block!
    [
      size ablak_c/size + p1 + 0x200
      style t_nev text right 90x24
      style tlab text with [ edge: [color: coal size: 1x1 ] font: [ space: 0x-2 ] para: [ origin: 1x2 ] ]
      backtile polished color_f4
      across space -1x8
      at 9x9 button close_btn_img 26x26
      [
        faktura_sif: copy ""
        ablak_cezar
        ablak_brajz
      ]
      at  40x10 text right "Faktura:"   70x24 
      at 110x10 info center rek/br_rac 120x24 color_g2
      at 240x10 button "Modif" 100x24 color_m2
      [
        if not write_mode [ exit ]
        if rek/faza > "0" [ alert "Faktura zatvorena!" exit ]
        faktura_kiiras
        ; azert hogy a nyomtatas is mehessen:
        mod_faktura
      ]
      at 430x10 button "Brisanje" 100x24 color_m4
      [
        if not write_mode [ exit ]
        if rek/faza >= "G" [ alert "Faktura je zatvorena!" exit ]
        if (rek/tip_fak = "R") and (rek/br_dok <> "") [ alert rejoin [ "Faktura je vezana za otpremnicu: " rek/br_dok ] exit ]
        if (request/confirm rejoin ["Brisati fakturu " faktura_sif " ?"]) <> true [ exit ]
        mysql_cmd [ "DELETE FROM faktura WHERE br_rac='" faktura_sif "' " ]
        mysql_cmd [ "DELETE FROM prom_fakt WHERE br_fak='" faktura_sif "' " ]
        mysql_cmd [ "DELETE FROM dok_fakt WHERE br_fak='" faktura_sif "' " ]
        mysql_cmd 
        [ 
          "UPDATE prodaja_serija SET br_fak='' "
          "WHERE br_prod='" rek/veza_dok "' AND br_fak='" faktura_sif "' "
        ]
        mysql_cmd 
        [ 
          "UPDATE dokument SET br_ul_rac='' "
          "WHERE br_dok='" rek/br_dok "' "
        ]
        faktura_sif: copy ""
        ablak_cezar
        ablak_brajz
      ]
      at  10x40 t_nev right "Datum:"   100x24
      at 110x40 w_date rek/datum       120x24
      at 250x40 text right "Faza:"      60x24
      at 310x40 info center (rek/faza)  30x24 color_g2
      at  10x70 t_nev "Rok isplate:"   100x24
      at 110x70 field rek/rok 40x24
      [
        if rek/datum/1 > #"0"
        [
          rek/dat_val: to-iso-date ( (to-date rek/datum ) + (int_value rek/rok) )
          mod_faktura
        ]
      ]  
      at 150x70 text center "dana" 40x24 
      at 220x70 text right "Dat.valute:" 90x24 
      at 310x70 info rek/dat_val 100x24 color_g2
    ]
    either rek/faza > "0"
    [
      append tabla_blk compose
      [
        at 350x40 button "Otvori" 60x24 color_m2
        [
          if not write_mode [ alert "Nije dozvoljeno!" exit ]
          if rek/faza > "G" [ alert "Nije dozvoljeno!" exit ]
          rek/faza: copy ""
          mysql_cmd [ "UPDATE faktura SET faza='" rek/faza "' WHERE br_rac='" faktura_sif "' " ]
          mod_faktura
        ]
      ]
    ]
    [
      append tabla_blk compose
      [
        at 350x40 button "Gotov" 60x24 color_m2
        [
          rek/faza: copy "G"
          mysql_cmd [ "UPDATE faktura SET faza='" rek/faza "' WHERE br_rac='" faktura_sif "' " ]
          mod_faktura
        ]
      ]
    ]
    append tabla_blk compose
    [
      at 420x40 text right "Tip:" 50x24
      at 470x40 info center rek/tip_fak 30x24 color_g2
    ]
    ;
    blk: compose
    [
      size 520x100 across
      backtile polished color_g4
      at 10x10 t_nev "Otpremnica:" 100x24
      at 110x10 arrow right 24x24 color_l2
      [
        tabla_keres "dokum_izb"
        tabla/value: reduce [ rek/br_dok ]
        tabla/filter: copy ""
        izbor_sifre [ 'btn "Bris.vezu" ]
        if retval = "Bris.vezu"
        [
          rek/br_dok: copy ""
          rek/datum_dok: copy ""
          mod_faktura
        ]
        if block? retval
        [
          rek/br_dok: copy retval/1
          mysql_cmd
          [
            "SELECT datum_dok,sif_mag,sif_part,veza_dok FROM dokument WHERE br_dok='" rek/br_dok "' "
          ]
          if not empty? row1: first db
          [
            rek/datum_dok: copy row1/1/1
            rek/sif_mag:   copy row1/1/2
            rek/sif_part:  copy row1/1/3
            rek/veza_dok:  copy row1/1/4
            mod_faktura
          ]
        ]
        tabla_keres "faktura_izb"
      ]
      at 140x10 info rek/br_dok 120x24 color_g2
      at 10x40 t_nev "Datum dok.:" 100x24 
      at 110x40 info rek/datum_dok 90x24 color_g2
      at 240x40 t_nev "Magacin:" 100x24 
      at 340x40 info center rek/sif_mag 40x24 color_g2
      at 10x70 t_nev "Kupac:" 100x24
      at 110x70 info center rek/sif_part 70x24 color_g2
      at 190 text pnev 250x24
      [
        if rek/sif_part <> ""
        [
          insert nyomtar reduce [ compose [ ~"Dom-Faktura" (rek/br_rac) ] ]
          nyomelem: reduce [ rek/sif_part ]
          program_nev: ~"Partner" program_indito
        ]
      ]
    ]
    if (rek/tip_fak <> "") and (rek/sif_fak <> "")
    [
      append blk compose
      [
        at 10x40 t_nev "Datum dok.:" 100x24 
        at 110x40 w_date rek/datum_dok 120x24
      ]
    ]
    if rek/br_dok <> ""
    [
      append blk compose
      [
        at 280x10 button "Otpremnica" 100x24 color_l2
        [
          insert nyomtar reduce [ compose [ ~"Dom-Faktura" (faktura_sif) ] ]
          nyomelem: reduce [ rek/br_dok ]
          program_nev: ~"Knjiženje-mag" program_indito
        ]
        at 10x40 t_nev "Datum dok.:" 100x24 
        at 110x40 info center rek/datum_dok 100x24 color_g2
      ]
    ]
    if (rek/tip_fak <> "") and (rek/sif_fak <> "") and (rek/br_dok = "")
    [
      append blk compose
      [
        
      ]
    ]
    if (rek/tip_fak <> "") and (rek/sif_fak <> "") and (rek/br_dok = "")
    [
      append blk compose
      [
        at 400x10 button "Kreir.Otpr." 100x24 color_m2
        [
          if not write_mode [ alert "Nije dozvoljeno!" exit ]
          if rek/faza > "0" [ alert "Nije dozvoljeno!" exit ]
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
          row1/datum_dok: copy rek/datum_dok
          row1/sif_mag: copy rek/sif_mag
          kreiranje_otpremnice row1
          ;
          rek/br_dok: copy row1/br_dok
          faktura_kiiras
          mod_faktura
        ]
      ]
    ]
    if (rek/tip_fak <> "") and (rek/sif_fak <> "")
    [
      append blk compose
      [
        at 400x40 button "Promet" 80x24 color_b2
        [
          if not write_mode [ alert "Nije dozvoljeno!" exit ]
          if rek/faza > "0" [ alert "Nije dozvoljeno!" exit ]
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
    append tabla_blk compose
    [
      at   5x100 panel blk with [ edge: [color: coal size: 1x1 ]]
      at  10x220 t_nev "Naèin plaæanja:" 120x24 
      at 130x220 field rek/namena        120x24
      at 250x220 text right "Naziv2:"    100x24
      at 350x220 check (naziv2)           24x24
      [
        either naziv2 
        [
          naziv2: false
          rek/na1: copy ""
        ]
        [
          naziv2: true
          rek/na1: copy "d"
        ]
        mysql_cmd [ "UPDATE faktura SET na1='" rek/na1 "' WHERE br_rac='" rek/br_rac "' " ]
      ]
      at 390x220 btn "Štamp.Fakture"     140x24 color_l2
      [
        stamp_fakture rek/br_rac
      ]
      at  10x250 t_nev "Otpreme:"   120x24
      at 130x250 field rek/otprema  120x24
      at 250x250 t_nev "Iznos:"     100x24
      at 350x250 info right (dec_form rek/iznos 0 2)   140x24 color_g2
      at  10x280 t_nev "Prevoza:"   120x24
      at 130x280 field rek/prevoz   120x24
      at 250x280 t_nev "Porez:"     100x24
      at 350x280 info right (dec_form rek/osn_por 0 2) 140x24 color_g2
      at  10x310 t_nev "Troškovi:"  120x24
      at 130x310 field rek/troskovi 120x24
      at 250x310 t_nev "Ukupno:"    100x24
      at 350x310 info right (dec_form rek/ukupno 0 2)  140x24 color_g2
      at  10x340 t_nev "Rabat:"     120x24
      at 130x340 field rek/rabat    120x24
      at 250x340 t_nev "Dok.EP:"     70x24 
      at 320x340 arrow right 24x24 color_l2
      [
        if rek/faza > "0" [ alert "Nije dozvoljeno!" exit ]
        blk1: parse/all rek/veza_dok "."
        either (length? blk1) = 2
        [
          tabla/value: reduce [ blk1/1 blk1/2 ]
          tabla/filter: rejoin [ "serija=" apjel (blk1/2) apjel ]
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
      at 350x340 info rek/veza_dok 140x24 color_g2
      at 490x340 btn "Dok" 40x24 color_l2
      [
        row1: otpremnica_prodaje_keres  
        if not retval [ alert "Faktura nije upisana kod prodaje!" exit ]
        insert/only nyomtar reduce [ program_nev faktura_sif ]
        blk1: parse/all rek/veza_dok "."
        either (length? blk1) = 2
        [
          nyomelem: reduce [ blk1/1 blk1/2 ]
        ]
        [
          nyomelem: reduce [ rek/veza_dok "0" ]
        ]
        program_nev: ~"Evidencija-Prodaje" program_indito
      ]
      at   2x380 t_nev "Opis:"       40x24
      at  40x380 field rek/opis     490x24
      at  40x410 field rek/opis1    490x24
      at  40x440 field rek/opis2    490x24
    ]
    append tabla_blk compose
    [
      at 10x480 t_nev "Inserted:" 70x24
      info rek/kor_insert 70x24 color_g2
      info rek/dat_insert 95x24 color_g2
      t_nev "Modified:"   70x24
      info rek/kor_modif  70x24 color_g2
      info rek/dat_modif  95x24 color_g2
    ]
    p1: 10x520
    append tabla_blk compose
    [
      at (p1) tlab center "Dodatni trošak" 421x26 color_h1 color_h2
      at (p1 + 420x0) tlab "Cena" 101x26 color_h1 color_h2
    ]
    p1: p1 + 0x25
    ;
    mysql_cmd
    [
      "SELECT * FROM kalk_dokumenta WHERE br_dok='" rek/br_dok "' AND tip='TR' ORDER BY rbr ASC "
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
        tlab (row1/naziv) 371x26 color_t1 color_t2
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
        if (not string? rek/br_dok) or (rek/br_dok = "") [ alert "Fali otpremnica!" exit ]
        novi_red_troska
      ]
    ]
    ;
    p2: 10x100
    blk_upl: compose
    [
      size (p2 + 510x130)
      across
      backtile polished color_g4
      at 10x10 t_nev left bold "Evidencija uplate:" 200x24
    ]
    append blk_upl compose
    [
      at  10x40 t_nev left "Datum pl." 120x24 
      at 140x40 t_nev left "Iznos" 120x24
      at  10x70 t_date: w_date (sistime/isodate) 120x24
      at 140x70 t_iznos: field 120x24
      at 280x70 btn "Novi red" 120x24 color_t2
      [
        if not write_mode  [ alert "Nije dozvoljeno!" exit ]
        k1: 0.0
        ;if rek/faza >= "G" [ alert "Nije dozvoljeno!" exit ]
        if (t_iznos/text = "0") or (t_iznos/text = "") [ alert "Nije dozvoljeno!" exit ]
        k1: round/to (dec_value t_iznos/text) 0.01
        saldo: round/to saldo 0.01
        if (saldo = 0) or (k1 > saldo)
        [
          t_iznos/text: copy ""
          alert "Nije dozvoljeno! Saldo manje od iznos uplata!"
          mod_faktura
          focus t_iznos
          exit
        ]
        mysql_cmd
        [
          "INSERT INTO upl_fakt SET br_fak='" faktura_sif "',datum='" t_date/text "',uplata='" (dec_value t_iznos/text) "',tip='I',"
          "sif_part='" rek/sif_part "',vrsta='R',godina='" god0 "'"
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
        if (copy/part rek/datum 4) = baze_sif
        [
          mysql_cmd
          [
            "UPDATE dok_fakt SET uk_upl='" uk_upl "',ino_upl='" ino_upl "' WHERE br_fak='" faktura_sif "' "
          ]
        ]
        ;
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
      "SELECT mbr,datum,uplata,tip,sif_part FROM upl_fakt "
      "WHERE br_fak='" rek/br_rac "' AND vrsta<>'U' AND vrsta<>'P'"
    ]
    tar2: get_rekordset copy db
    p2: p2 + 0x37
    saldo0: (dec_value rek/ukupno)
    foreach row2 tar2
    [
      uk_uplata: (dec_value uk_uplata) + (dec_value row2/uplata)
      saldo: saldo0 - (dec_value uk_uplata)
      plata_rok:  (to-date row2/datum) - (to-date rek/datum)
      plata_dfak: (to-date row2/datum) - (to-date rek/dat_val)
      sz1: rejoin [ plata_rok " / " plata_dfak " dan" ]
      append blk_upl compose/only
      [
        at (p2)
        tlab center (row2/datum) 110x25 color_b1 color_b2
        with (compose/only [ data: (row2) ])
        [
          if not write_mode  [ alert "Nije dozvoljeno!" exit ]
          row1: face/data
          if (request/confirm rejoin [ "Brisati uplatu " (money_form row2/uplata 2) "?" ]) <> true [ exit ]
          mysql_cmd
          [
            "DELETE FROM upl_fakt WHERE br_fak='" rek/br_rac "' AND mbr='" row2/mbr "' AND tip='I' AND vrsta='R' "
          ]
          mod_faktura
        ]
        
        tlab right (money_form row2/uplata 2) 120x25 color_t1 color_t2
        tlab right (money_form saldo 2)       120x25 color_t1 color_t2
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
      tlab right (money_form saldo 2)     120x25 color_h1 color_h2
    ]
    p2: p2 + 0x30
    append blk_upl compose
    [
      at (p2) t_nev right "Saldo:" 90x24
      at (p2 + 90x0) info right to-string (money_form saldo 2) 150x24 color_g2
    ]
    p2: p2 + 0x30
    append blk_upl compose
    [
      at (p2) text right "Napomena:"  90x24 
      at (p2 + 90x0) field rek/opis  390x24
    ]
    append tabla_blk compose
    [
      at (p1 + 0x50) panel blk_upl with [ edge: [color: coal size: 1x1 ]] 
    ]
    append tabla_blk compose
    [
      ;at (p1 + -5x620) panel with [ edge: [color: coal size: 1x1 ]]
      at (p1 + p2 + -10x100) panel with [ edge: [color: coal size: 1x1 ]]
      [
        size 520x345 across
        backtile polished color_g4
        at  30x20 text right "Avansni raèun 1:" 130x24 at 160x20 field rek/predracun 120x24
        at 280x20 t_nev "Datum:"  90x24
        at 370x20 w_date rek/dat_avans 120x24
        at  10x50 t_nev "Tarifa 20 -> Avans:" 150x24
        at 160x50 field rek/avans18 120x24
        at 280x50 t_nev "Pdv 20:"    90x24
        at 370x50 field rek/pdv18   120x24
        at  10x80 t_nev "Tarifa 10 -> Avans:" 150x24
        at 160x80 field rek/avans8  120x24
        at 280x80 t_nev "Pdv 10:"    90x24
        at 370x80 field rek/pdv8    120x24
        ;
        at  30x130 text right "Avansni raèun 2:" 130x24
        at 160x130 field rek/predracun_1  120x24
        at 280x130 t_nev "Datum Av.:"      90x24
        at 370x130 w_date rek/dat_avans_1 120x24
        at  10x160 t_nev "Tarifa 20 -> Avans:" 150x24
        at 160x160 field rek/avans18_1    120x24
        at 280x160 t_nev "Pdv 20:"         90x24
        at 370x160 field rek/pdv18_1      120x24
        at  10x190 t_nev "Tarifa 10 -> Avans:" 150x24
        at 160x190 field rek/avans8_1     120x24
        at 280x190 t_nev "Pdv 10:"         90x24
        at 370x190 field rek/pdv8_1       120x24
        ;
        at  30x240 text right "Avansni raèun 3:" 130x24
        at 160x240 field rek/predracun_2  120x24
        at 280x240 t_nev "Datum Av.:"      90x24
        at 370x240 w_date rek/dat_avans_2 120x24
        at  10x270 t_nev "Tarifa 20 -> Avans:" 150x24
        at 160x270 field rek/avans18_2    120x24
        at 280x270 t_nev "Pdv 20:"         90x24
        at 370x270 field rek/pdv18_2      120x24
        at  10x300 t_nev "Tarifa 10 -> Avans:" 150x24
        at 160x300 field rek/avans8_2     120x24
        at 280x300 t_nev "Pdv 10:"         90x24
        at 370x300 field rek/pdv8_2       120x24
      ]  
    ]
    ablak_c/pane: layout/offset tabla_blk 0x0
    show ablak_c
  ]
  
  otpremnica_prodaje_keres: func [ ]
  [
    either rek/br_dok = ""
    [
      mysql_cmd
      [
        "SELECT br_prod,serija FROM prodaja_serija WHERE br_fak='" faktura_sif "' "
      ]
      return get_rekord first db
    ]
    [
      mysql_cmd
      [
        "SELECT br_prod,serija FROM prodaja_serija WHERE dok_otprem='" rek/br_dok "' "
      ]
      return get_rekord first db
    ]
  ]

  ablak_brajz: func [ /local tar0 tar1 poz0 ]
  [
    
    ; tabla - kereso beallitasai:
    tabla_keres "faktura_izb"
    tabla/akcio: func [ adat ]
    [
      tabla_keres "faktura_izb"
      faktura_sif: copy adat/1
      mysql_cmd [ "SELECT * FROM faktura WHERE br_rac = '" faktura_sif "' " ]
      rek: get_rekord first db
      if not retval
      [ 
        alert "Faktura nije u bazi!" 
        faktura_sif: copy ""
        ablak_brajz exit
      ]
      row1: otpremnica_prodaje_keres
      rek/veza_dok: copy row1/br_prod 
      mod_faktura
      ablak_brajz
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
      text nazad_nev 200x24
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

  set 'faktura_izb: func [ /local ]
  [
    faktura_sif: copy ""
    ;
    mode: make object!
    [ 
      dat_od_ios:  sistime/isodate
      dat_do_ios:  sistime/isodate
      dat_izv:     rejoin [ baze_sif "-12-31" ]
      vrsta_datum: copy ""
      partner:     copy ""
      partner0:    copy ""
      naz_part:    copy ""
    ]
    ;
    ablak_arajz
    ablak_cezar
    main_window/user-data: ablak_c ; gorgo-objektum
    if not empty? nyomelem
    [
      faktura_sif: copy nyomelem/1
      remove nyomelem
      mysql_cmd [ "SELECT * FROM faktura WHERE br_rac = '" faktura_sif "' " ]
      rek: get_rekord first db
      either retval [ mod_faktura ][ alert "Faktura nije u bazi !"  ]
    ]
    ablak_brajz
  ]
]

append cleanup
[
  faktura_def_01 
  faktura_izb
  rek
]
