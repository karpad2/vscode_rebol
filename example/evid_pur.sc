; PUR
evid_pur_def_01: context
[ 
  pl_dok:    none ;-- beszerzesi dokumentum szam
  sif_nar:   none
  prek:      none ; rekord pur
  ptar:      none
  ablak_p1:  none
  ablak_p2:  none
  ablak_q1:  none
  ablak_q2:  none
  ablak_q3:  none
  faza_blk:  none
  dat0:      none
  br_ugov:   copy ""
  
  kreir_anex1: func [ /local blk0 blk1 sz0 sz1 row0 sif0 novi_sif f1 ]
  [
    f1: false
    foreach [ sif0 row0 ] ptar
    [
      if row0/izbor = "1" [ f1: true  break ]
    ]
    if not f1 [ alert "Nema izabrana redova!" exit ]
    ;
    blk0: make block! []
    ;
    sz0: copy prek/br_ugov
    if find sz0 "-"
    [
      sz0: copy/part at sz0 1 (find sz0 "-")
    ]
    mysql_cmd [ "SELECT  br_fak FROM faktura2 WHERE br_fak LIKE '" sz0 "%' ORDER BY br_fak DESC LIMIT 1" ]
    row0: get_rekord first db
    ;
     
    blk0: parse/all row0/br_fak "-"
    either (length? blk0) < 2 [ append blk0 "1" ]
    [
      blk0/2: (int_value blk0/2) + 1
    ]
    sz0: blokk_pakolo blk0 "-"
    if string? sz0 [ novi_sif: copy sz0 ]
    sz1: request-text/title/default "Broj dokumenta:" sz0
    if not string? sz1 [ exit ]
    mysql_cmd [ "SELECT br_fak FROM faktura2 WHERE br_fak = '" sz1 "'" ]
    if (not empty? first db)
    [
      alert rejoin [ ~"Faktura:" " '" sz1 "' " ~"veæ postoji!" ] exit
    ]
    ;
    blk1: make block! []
    foreach [ sif0 row0 ] ptar
    [
      if row0/izbor = "1"
      [
        append blk1 row0/mbr
      ]
    ]
    ;
    mysql_cmd
    [
      "INSERT IGNORE INTO evid_pur SET br_ugov='" sz1 "',faza='" prek/faza "',vrsta='" prek/vrsta "',valuta='" prek/valuta "',"
      "dat_ugov='" prek/dat_ugov "',sif_mag='" prek/sif_mag "',dok_nabavke='" prek/dok_nabavke "',sif_part='" prek/sif_part "',"
      "br_odobr='" prek/br_odobr "',dat_odobr='" prek/dat_odobr "',br_fak='" sz1 "',dok_otprem='" prek/dok_otprem "',opis_otprem='',"
      "dat_otprem='" prek/dat_otprem "',otp_valuta='" prek/otp_valuta "',dat_istupa='" prek/dat_istupa "',otp_kurs='" prek/otp_kurs "',"
      "naslov='',lista_nar='" (blokk_pakolo prek/lista_nar " ") "',godina='" baze_sif "',kor_insert='" korisnik "',dat_insert=CURDATE() "
      "ON DUPLICATE KEY UPDATE faza='" prek/faza "',vrsta='" prek/vrsta "',valuta='" prek/valuta "',"
      "dat_ugov='" prek/dat_ugov "',sif_mag='" prek/sif_mag "',dok_nabavke='" prek/dok_nabavke "',sif_part='" prek/sif_part "',"
      "br_odobr='" prek/br_odobr "',dat_odobr='" prek/dat_odobr "',br_fak='" sz1 "',dok_otprem='" prek/dok_otprem "',opis_otprem='',"
      "dat_otprem='" prek/dat_otprem "',otp_valuta='" prek/otp_valuta "',dat_istupa='" prek/dat_istupa "',otp_kurs='" prek/otp_kurs "',"
      "naslov='',lista_nar='" prek/lista_nar "',godina='" baze_sif "',kor_modif='" korisnik "',dat_modif=CURDATE()"
    ]
    mysql_cmd
    [
      "INSERT IGNORE INTO faktura2 SET br_fak='" sz1 "',otp_valuta='EUR',otp_kurs='" firma_rek/kurs_euro "',"
      "best_nr='da',best_bk='ne',po_nar='da',zaglav='da',opis='',kor_insert='" korisnik "',dat_insert=CURDATE() "
    ]
    mysql_cmd compose
    [
      "UPDATE prom_pur AS t1 SET t1.br_ugov=" apjel (sz1) apjel " "
      "WHERE t1.br_ugov=" apjel (prek/br_ugov) apjel " AND t1.mbr IN (" (mylist_form blk1) ") AND t1.proforma=" apjel apjel " "
    ]
    mysql_cmd
    [
      "SELECT @sorsz:=0"
    ]
    mysql_cmd
    [
      "UPDATE prom_pur AS t1 SET red_br = ( SELECT @sorsz := @sorsz + 1 ) "
      "WHERE br_ugov='" prek/br_ugov "' AND proforma='' ORDER BY red_br ASC "
    ]
    mysql_cmd
    [
      "SELECT @sorsz:=0"
    ]
    mysql_cmd
    [
      "UPDATE prom_pur AS t1 SET red_br = ( SELECT @sorsz := @sorsz + 1 ) "
      "WHERE t1.br_ugov='" sz1 "' AND t1.proforma='' ORDER BY red_br ASC "
    ]
  ]
  
  kreir_anex: func [ /local blk0 blk1 sz0 sz1 row0 sif0 novi_sif f1 ser0 ]
  [
    f1: false
    ser0: 0
    ;
    foreach [ sif0 row0 ] ptar
    [
      if row0/izbor = "1" [ f1: true  break ]
    ]
    if not f1 [ alert "Nema izabrana redova!" exit ]
    ;
    blk0: make block! []
    ;-- kikeresem az utolso serija szamot
    mysql_cmd [ "SELECT MAX(serija) AS serija FROM evid_pur WHERE br_ugov='" prek/br_ugov "' LIMIT 1" ]
    row0: get_rekord first db
    if not retval [ alert "Greška!!!" exit ]
    ser0: int_value row0/serija
    ser0: ser0 + 1
    mysql_cmd [ "SELECT br_ugov FROM evid_pur WHERE br_ugov='" prek/br_ugov "' AND serija='" ser0 "' LIMIT 1" ]
    row0: get_rekord first db
    if retval [ alert "Serija veæ postoji!!!" exit ]
    ;
    blk1: make block! []
    foreach [ sif0 row0 ] ptar
    [
      if row0/izbor = "1"
      [
        append blk1 row0/mbr
      ]
    ]
    ;
    mysql_cmd
    [
      "INSERT IGNORE INTO evid_pur SET br_ugov='" prek/br_ugov "',serija='" ser0 "',faza='" prek/faza "',vrsta='" prek/vrsta "',"
      "valuta='" prek/valuta "',dat_ugov='" prek/dat_ugov "',sif_mag='" prek/sif_mag "',dok_nabavke='" prek/dok_nabavke "',"
      "sif_part='" prek/sif_part "',br_odobr='" prek/br_odobr "',dat_odobr='" prek/dat_odobr "',br_fak='',"
      "dok_otprem='" prek/dok_otprem "',opis_otprem='',dat_otprem='" prek/dat_otprem "',otp_valuta='" prek/otp_valuta "',"
      "dat_istupa='" prek/dat_istupa "',otp_kurs='" prek/otp_kurs "',naslov='',lista_nar='" (blokk_pakolo prek/lista_nar " ") "',"
      "godina='" baze_sif "',kor_insert='" korisnik "',dat_insert=CURDATE() "
      "ON DUPLICATE KEY UPDATE faza='" prek/faza "',vrsta='" prek/vrsta "',valuta='" prek/valuta "',"
      "dat_ugov='" prek/dat_ugov "',sif_mag='" prek/sif_mag "',dok_nabavke='" prek/dok_nabavke "',sif_part='" prek/sif_part "',"
      "br_odobr='" prek/br_odobr "',dat_odobr='" prek/dat_odobr "',br_fak='',dok_otprem='" prek/dok_otprem "',opis_otprem='',"
      "dat_otprem='" prek/dat_otprem "',otp_valuta='" prek/otp_valuta "',dat_istupa='" prek/dat_istupa "',otp_kurs='" prek/otp_kurs "',"
      "naslov='',lista_nar='" prek/lista_nar "',godina='" baze_sif "',kor_modif='" korisnik "',dat_modif=CURDATE()"
    ]
    mysql_cmd compose
    [
      "UPDATE prom_pur AS t1 SET t1.br_ugov=" apjel (prek/br_ugov) apjel ",t1.serija=" apjel (ser0) apjel " "
      "WHERE t1.br_ugov=" apjel (prek/br_ugov) apjel " AND t1.serija='" prek/serija "' AND t1.mbr IN (" (mylist_form blk1) ") AND t1.proforma=''"
    ]
    mysql_cmd
    [
      "SELECT @sorsz:=0"
    ]
    mysql_cmd
    [
      "UPDATE prom_pur AS t1 SET red_br = ( SELECT @sorsz := @sorsz + 1 ) "
      "WHERE br_ugov='" prek/br_ugov "' AND serija='" ser0 "' AND t1.proforma='' ORDER BY t1.red_br ASC "
    ]
    
  ]
  
  upis_nabav_dokum: func [ /local part0 doknab_sif sz1 sz2 dat0 tar0 tar1 row0 row1 f1 n1 n2 nab_dat ]
  [
    nab_dat: sistime/iso
    mysql_cmd compose
    [
      "SELECT * FROM dok_nab WHERE br_dok IN (" (mylist_form prek/dok_nabavke) ") "
    ]
    tar0: get_rekordset copy db
    ;
    clear prek/dok_nabavke
    ;
    foreach row0 tar0
    [
      if none? row1: oblik_dokum_rekord "NAB" "NAB" [ alert "Tip dokumenta nije dobar!" exit ]
      doknab_sif: dokumentum_szam_keszito row1/dok_form1 row0/datum_dok "dok_nab" "br_dok"
      ;
      if not string? doknab_sif [ exit ]
      ;
      doknab_sif: request-text/title/default "Novi broj dokumenta: " doknab_sif
      if not string? doknab_sif [ exit ]
      if (length? doknab_sif) < 5 [ alert "Broj dokumenta minimium 5 slova!" exit ]
      ;
      mysql_cmd [ "SELECT * FROM dok_nab WHERE br_dok = '" doknab_sif "' " ]
      if not empty? row1: first db
      [
        alert "Mora biti nov dokument!" exit
      ]
      either prek/tip_p = "I"
      [
        sz1: copy "INO"
        sz2: copy "EUR"
      ]
      [
        sz1: copy "DOM"
        sz2: copy "RSD"
      ]
      ;
      mysql_cmd
      [
        "INSERT INTO dok_nab SET "
        "br_dok='" doknab_sif "',tip_dok='O',faza_dok='1',opis_dok='" row0/br_dok "',tip_zaht='" row0/tip_zaht "',"
        "sif_part='" prek/sif_part "',naz_part='" prek/naz_part "',rok_ispor='" row0/rok_ispor "',vrsta='" sz1 "',"
        "poslat='" row0/poslat "',valuta='" sz2 "',datum_dok='" nab_dat "',dok_dobav='" row0/dok_dobav "',"
        "opis='',dob_pon='" row0/dob_pon "',kor_insert='" korisnik "',dat_insert='" nab_dat "' "
      ]
      if not mysts
      [
        alert rejoin [ "Greška: Dokument nije upisan:" doknab_sif " !" ]
        exit
      ]
      mysql_cmd
      [
        "UPDATE nabavka SET br_dok='" doknab_sif "' WHERE br_dok='" row0/br_dok "' "
      ]
      if not mysts 
      [ 
        alert rejoin [ "Stavke nisu stavljene na dokument:" doknab_sif " !" ]
        exit 
      ]
      f1: 0 if firma_rek/zaht_import_mode = "T" [ f1: 1 ]
      mysql_cmd
      [
        "UPDATE prom_potnab SET dok_nabavke='" doknab_sif "',flag='" f1 "' "
        "WHERE dok_nabavke='" row0/br_dok "' "
      ]
      if not mysts 
      [ 
        alert rejoin [ "Stavke nisu skinute sa dokumenta:" row0/br_dok " !" ]
        exit 
      ]
      mysql_cmd
      [
        "DELETE FROM dok_nab WHERE br_dok='" row0/br_dok "' "
      ]
      append prek/dok_nabavke reduce [ doknab_sif ]
    ]
  ]
  
  dokument_adatok_feltolt: func [ blk0[block!] br_ugov[string!] serija[string!] /local blk1 blk2 blk3 tar0 row0 ]
  [
    blk1: make block! []
    blk2: make block! []
    blk3: make block! []
    blk4: make block! []
    ;
    mysql_cmd compose
    [
      "SELECT t1.br_dok,t1.sif_part,"
      "(SELECT GROUP_CONCAT(DISTINCT t2.br_dok ORDER BY t2.br_dok ASC SEPARATOR " apjel " " apjel ") "
       "FROM prom_potnab AS t2 WHERE t2.dok_nabavke=t1.br_dok) AS lista_zaht,"
      "(SELECT GROUP_CONCAT(DISTINCT t2.sifra_nar ORDER BY t2.br_dok ASC SEPARATOR " apjel " " apjel ") "
       "FROM prom_potnab AS t2 WHERE t2.dok_nabavke=t1.br_dok) AS lista_nar "
      "FROM dok_nab AS t1 "
      "WHERE t1.br_dok IN (" (mylist_form blk0) ")"
    ]
    tar0: get_rekordset copy db
    foreach row0 tar0
    [
      append blk1 row0/br_dok
      append blk2 reduce (parse row0/lista_zaht none)
      append blk3 reduce (parse row0/lista_nar  none)
      append blk4 row0/sif_part
    ]
    blk1: unique blk1
    blk2: unique blk2
    blk3: unique blk3
    blk4: unique blk4
    ;
    sif_part: copy ""
    if (length? blk4) = 1
    [
      mysql_cmd compose
      [
        "SELECT t1.sifra,t1.name1,t1.ozn_kupca,t1.mesto,t1.ulica,t1.fax,t1.telefon,t1.pib,t1.email "
        "FROM " firma/data ".partners AS t1 WHERE t1.sifra=" apjel (blk4/1) apjel " LIMIT 1"
      ]
      row1: get_rekord first db
      sif_part: copy row1/sifra
    ]
    ;
    mysql_cmd
    [
      "UPDATE evid_pur SET dok_nabavke='" (blokk_pakolo blk1 " ") "',sif_part='" sif_part "',lista_nar='" (blokk_pakolo blk3 " ") "' "
      "WHERE br_ugov='" br_ugov "' AND serija='" serija "' "
    ]
    mysql_cmd compose
    [
      "UPDATE dok_nab AS t1 SET t1.dok_dobav=" apjel (br_ugov) apjel " WHERE t1.br_dok IN (" (mylist_form blk0) ")"
    ]
  ]
  
  pur_korpa_keres: func [ br_ugov[string!] serija[string!] /local blk0 ]
  [
    blk0: make block! []
    ;
    tabla_keres "pregled_pur_nabkorpa"
    tabla/fejlec: ~"Izbor PUR nabav.korpa"
    tabla/name:   copy "dok_nab AS t1"
    tabla/lista:  rejoin
    [
      "t1.br_dok|"
      "IFNULL((SELECT t3.sifra_nar FROM prom_potnab AS t3 "
      "WHERE t3.dok_nabavke=t1.br_dok ORDER BY sifra_nar ASC LIMIT 1)," apjel "" apjel") AS sifra_nar|"
      "(SELECT COUNT(*) FROM prom_potnab AS t2 WHERE t2.dok_nabavke=t1.br_dok) AS sorok1|t1.sif_part|t1.naz_part|"
      "IFNULL((SELECT t4.mesto FROM " firma/data ".partners AS t4 WHERE t4.sifra=t1.sif_part)," apjel "" apjel ") AS mesto"
    ]
    tabla/nevek: ~"Dok nabavke|Šifra nar.|Broj stavke|Partner|Naziv partnera|Mesto"
    tabla/oszlop: [   120         95          60         70        220        150 ]
    tabla/tipus:  [   "LS"       "CS"        "CI"       "CS"       ""         ""  ]
    tabla/order:  [ 1 "ASC" ]
    tabla/index:  [ 1 ]
    tabla/filter: rejoin [ "t1.tip_zaht IN (" apjel "P" apjel "," apjel "PI" apjel ") " ]
    ;
    if string? tabla_korpe/fejlec
    [
      clear tabla_korpe/data
    ]
    otvaranje_korpe
    ;-- kesobb jon ha lesz vegleges koncepcio
    izbor_sifre [ 'btn "Izaberi" ]
    ;
    if block? retval
    [
      append blk0 retval/1
      dokument_adatok_feltolt blk0 br_ugov serija
      ;return blk0
    ]
    ;-- kesobb lesz erdekes
    if string? retval
    [
      poz2: head tabla_korpe/data
      while [ not tail? poz2 ]
      [
        row4: poz2/2
        append blk0 row4/1
        poz2: next next poz2
      ]
      dokument_adatok_feltolt blk0 br_ugov serija
    ]
    tabla/pane: none
  ]
  
  ablak_p1rajz: func [ /local ablak_title ]
  [
    ablak_title: rejoin [ "Arhiviranje dokumenata za: " prek/br_ugov  ]
    ;
    tabla_blk: compose/only
    [
      size 940x640
      backdrop color_g4
      across space -1x4
      at 9x9 button close_btn_img 26x26 keycode escape
      [
        hide-popup
      ]
      at (as-pair -1 -1) ablak_p2: box color_g4 942x642 with [ edge: [ color: coal size: 1x1 ] ]
    ]
    ablak_p1: layout/offset tabla_blk 0x0
    ablak_p2/user-data: 0x0
    ablak_evid_pur
    ;
    inform/title ablak_p1 (ablak_title)
  ]
  
  ablak_evid_pur: func [ /local sz1 ]
  [
    blk_ext: make block! []
    sz1:     copy ""
    filter:  copy ""
    mysql_cmd compose
    [
      "SELECT DISTINCT t1.file_ext FROM " firma/data ".arhiva_pon AS t1 "
      "WHERE t1.sifra_nar='' "
    ]
    blk_ext: get_list copy db
    blk_ext: unique blk_ext
    sort/skip blk_ext 1
    insert blk_ext sz1
    tabla_blk: compose
    [
      size ablak_p2/size
      style tlab text with [ edge: [color: coal size: 1x1 ] font: [ space: 0x-2 ] para: [ origin: 1x2 ] ]
      backdrop color_g4
      across space -1x4
      at 9x9 button close_btn_img 26x26 keycode escape
      [
        hide-popup
      ]
      at  40x10 text "<ESC>" 60x24
      at 100x10 text right "Broj dokumenta:" 150x24
      at 250x10 info center prek/br_ugov  140x24 color_g2
      at 450x10 text right "Dodati prilog sa diska:" 150x24
      at 600x10 button "Arhivirati" 100x24 color_b2
      [
        dodati_prilog_disk
        ablak_evid_pur
      ]
      at 10x50
      tlab center "Prilog-file" 461x38 color_h1 color_h2 
      tlab center "File^/verz"   41x38 color_h1 color_h2 
      tlab center "Br.dokum"    141x38 color_h1 color_h2
      tlab center "Dat.insert"  101x38 color_h1 color_h2
      tlab center "Kor.arhiv."  161x38 color_h1 color_h2
      tlab center ""  20x38 color_h1 color_h2
      ;
      at  10x87 ablak_q1: box color_g4 901x540 with [ edge: [ color: coal size: 1x1 ] ]
      at 910x87 slider 21x540 with [ user-data: ablak_q1 ]
      [ scroller_pozicio face ]
    ]
    ablak_p2/pane: layout/offset tabla_blk 0x0
    ablak_q1/user-data: 0x0
    show ablak_p2
    arhiva_prilog
    ;
    ablak_p2/user-data: ablak_q1 ; gorgetheto-objektum
  ]
  
  
  arhiva_prilog: func [ /local row1 tar0 p1 ]
  [
    mysql_cmd compose
    [
      "SELECT t1.*,"
      "IFNULL((SELECT IFNULL((SELECT CONCAT(TRIM(t3.prezime),' ',TRIM(t3.ime)) FROM " firma/data ".zaposleni AS t3 WHERE t3.matbr=t2.mbr),'') "
              "FROM " firma/data ".korisnik AS t2 WHERE t2.korisnik=t1.insert_kor),'') AS kor_arh "
      "FROM " firma/data ".arhiva_pon AS t1 "
      "WHERE t1.sifra_nar='' " (filter)
      "ORDER BY t1.insert_date DESC,t1.insert_time DESC,t1.insert_rbr DESC "
    ]
    tar0: get_rekordset copy db
    p1: -1x-1
    tabla_blk: compose
    [
      size p1
      style tlab text with [ edge: [color: coal size: 1x1 ] font: [ space: 0x-2 ] para: [ origin: 1x2 ] ]
      backdrop color_g4
      across space -1x4
    ] 
    
    foreach row1 tar0
    [
      append tabla_blk compose/only
      [
        at (p1) tlab (rejoin [ row1/file_name "." row1/file_ext ]) 461x28 color_b1 color_b2
        with (compose/only [ data: (row1) ])
        [
          row1: face/data
          arhiva_kontrol row1
        ]
        tlab center (row1/file_verz)    41x28 color_t1 color_t2
        tlab center (row1/sifra_nar)   141x28 color_t1 color_t2
        tlab center (row1/insert_date) 101x28 color_t1 color_t2
        tlab left   (row1/kor_arh)     161x28 color_t1 color_t2
      ]
      p1: p1 + 0x27
    ]
    p1: max (p1 + 0x30) ablak_q1/size
    ablak_q1/pane: layout/offset tabla_blk ablak_q1/user-data
    show ablak_q1
  ]
  
  dodati_prilog_disk: func [ /local dok_path row0 podmapa1 sz1 ]
  [
    podmapa1: copy ""
    dok_path: request-file/title/filter/file/keep/only "Izbor documenta" "Izbor" "*.*"  dir_special
    if not file? dok_path [ exit ]
    if find dok_path ".eml" [ alert "Arhiviranje mailova nije dozvoljeno!!!" exit ]
    ;
    sz1: copy (uppercase "IZ-8101")
    replace sz1 "/" "-"
    replace sz1 "." "_"
    ;
    row0: make object! compose
    [
      dok_path:   (to-string dok_path)
      arhiv_map:  (copy sz1)
      veza_sif:   (copy "")
      podmapa:    (copy "") 
      sif_robe:   (copy "")
      naz_robe:   (copy "") 
      sifra_nar:  (copy "") 
      datum_dok:  (copy "") 
      insert_rbr:  none
    ]
    arhiviranje_dokumenta_pon row0
  ]
  
  arhiviranje_dokumenta_pon: func [ rek1[object!] /local dokum_sif sif_map naz0 f_name f_verz f_size f_path n1 poz0 rek2 blk0 ]
  [
    if (get in file_server 'access) = 0 [ alert "Nije dozvoljeno!" exit ]
    dokum_sif: copy "pon"
    ;
    sif_map: to-string baze_sif
    naz0:    copy rek1/arhiv_map
    ;----------
    mysql_cmd
    [
      "SELECT * FROM " firma/data ".grupa_dokum WHERE grupa_dok='" dokum_sif "' "
    ]
    rek2: get_rekord first db
    if not retval [ alert rejoin [ "Arhiva ( " (dokum_sif) " ) nije definisana!" ] exit ]
    rek2/file_tips: parse rek2/file_tips none
    ;
    ; f_name es f_ext meghatarozasa:
    ;
    f_name: copy last parse/all rek1/dok_path "/"
    poz0: find/last f_name "."
    if none? poz0 [ alert "File-tip nije odreðen !" exit ]
    f_ext: trim lowercase copy next poz0
    if f_ext = "" [ alert "File-tip nije odreðen !" exit  ]
    if (length? f_ext) > 10 [ alert "File-tip nije dobar !" exit  ]
    if not find rek2/file_tips "*"
    [
      if not find rek2/file_tips f_ext
      [
        alert rejoin [ "Dozvoljeni file-tipovi: " form rek2/file_tips " !" ] exit
      ]
    ]
    clear poz0
    if find f_name "."
    [
      f_name: copy/part at f_name 1 ((index? find/last f_name ".") - 1)
    ]
    if f_name = "" [ retval: false alert "Naslov nije odreðen !" exit ]
    mysql_cmd
    [
      "SELECT MAX(file_verz) FROM " firma/data ".arhiva_" dokum_sif " "
      "WHERE sifra_mape='" sif_map "' AND naziv_mape='" naz0 "/' "
      "AND file_name='" f_name "' AND file_ext='" f_ext "' "
    ]
    n1: 0 if not empty? row0: first db [ n1: int_value row0/1/1 ]
    n1: n1 + 1
    f_verz: to-string n1
    ;
    if n1 > 1
    [
      if (request/confirm rejoin [ "Upisati verziju dokumenta " f_verz " u arhivu?" ]) <> true [ exit ]
    ]
    ; f_size:
    ;
    f_size: size? to-file rek1/dok_path
    ; direktory kontrol:
    blk0: reduce [ dokum_sif sif_map naz0 ]
    ftp_create_arhivpath rem_adr blk0 0
    ;
    if not retval [ alert "FTP-Server create-map error!" exit ]
    ;                 ip cim      pon             ev       bazisbol a sifra_nar/podmapa  falj nev falj verzio kiterjesztes 
    f_path: rejoin [ rem_adr "/" dokum_sif "/" sif_map "/" naz0 "/" f_name "." f_verz "." f_ext ]
    ;
    ftp_copy_file to-file rek1/dok_path f_path ; kopiroz az arhivaba
    if not retval [ alert "FTP-Server write error!" exit ]
    ;
    ; ha sikerult, beirni a tablaba is
    ;
    mysql_cmd
    [
      "INSERT INTO " firma/data ".arhiva_" dokum_sif " "
      "SET insert_kor='" korisnik "',insert_date=CURDATE(),insert_time=CURTIME(),"
      "file_verz='" f_verz "',file_path='" dokum_sif "/" sif_map "/" naz0 "/',file_name='" f_name "',"
      "file_size='" f_size "',file_ext='" f_ext "',sifra_mape='" sif_map "',naziv_mape='" naz0 "/',"
      "old_comp='" system/network/host "^/" system/network/host-address "',old_path='" rek1/dok_path "',"
      "datum_dok='" rek1/datum_dok "',sifra_nar='" rek1/sifra_nar "' "
    ]
  ]
  
  ;-- archivalasi resz
  arhiva_kontrol: func [ row1[object!] /local f_path dok_path poz1 ]
  [
    tabla_blk: compose
    [
      size 500x130
      backdrop color_g4
      across
      at 9x9 button close_btn_img 26x26
      [
        hide-popup
      ]
      at 40x10 text (rejoin [ row1/file_name "." row1/file_ext ]) 400x28
      at 10x50 text "Brisati dokument iz arhive:" 240x24
      at 250x50 button "Brisati" 80x24 color_m4
      [
        if not write_mode [ exit ]
        brisanje_dokum_arhive "pon" row1
        hide-popup
        arhiva_prilog
      ]
      at 400x50 button "Prikaz" 80x24 color_l2
      [
        f_path: rejoin [ rem_adr "/" row1/file_path row1/file_name "." row1/file_verz "." row1/file_ext ]
        dok_path: to-file rejoin [ dir_lokal row1/file_name "." row1/file_ext ]
        ftp_copy_file f_path dok_path
        if not retval [ alert "FTP-Server read error!" exit ]
        ;
        dokument_megjelenito to-string dok_path
        hide-popup
      ]
    ]
    ablak_mp: layout/offset tabla_blk 0x0
    ;
    inform/title ablak_mp "Izbor"
  ]

  ispis_partner: func [ /local row1 ]
  [
    if not write_mode [ short_alert "Nije dozvoljeno!" exit ]
    prek/dat_modif: sistime/isodate
    prek/kor_modif: korisnik
    mysql_cmd
    [
      "UPDATE evid_pur SET dat_ugov='" prek/dat_ugov "',sif_part='" prek/sif_part "',naz_part='" prek/naz_part "',"
      "mesto='" prek/mesto "',adresa='" prek/adresa "',telefon='" prek/telefon "',fax='" prek/fax "',email='" prek/email "',"
      "ozn_kupca='" prek/ozn_kupca "',pib='" prek/pib "',kor_modif='" prek/kor_modif "',dat_modif='" prek/dat_modif "' "
      "WHERE br_ugov='" prek/br_ugov "' AND serija='" prek/serija "'"
    ]
  ]
  
  pur_kiir: func [ /nab /local sif0 row0 sz1 ptar ]
  [
    if not write_mode [ short_alert "Nije dozvoljeno!" exit ]
    prek/dat_modif: sistime/isodate
    prek/kor_modif: korisnik
    sz1: blokk_pakolo prek/dok_nabavke " "
    mysql_cmd
    [
      "UPDATE evid_pur SET dat_ugov='" prek/dat_ugov "',faza='" prek/faza "',vrsta='" prek/vrsta "',"
      "valuta='" prek/valuta "',sif_mag='" prek/sif_mag "',dok_nabavke='" sz1 "',sif_part='" prek/sif_part "',br_odobr='" prek/br_odobr "',"
      "dat_odobr='" prek/dat_odobr "',br_fak='" prek/br_fak "',dok_otprem='" prek/dok_otprem "',dat_otprem='" prek/dat_otprem "',"
      "otp_valuta='" prek/otp_valuta "',dat_istupa='" prek/dat_istupa "',otp_kurs='" prek/otp_kurs "',lista_nar='" prek/lista_nar "',"
      "plandat_isp='" prek/plandat_isp "',faza_priv='" prek/faza_priv "',faza_kon='" prek/faza_kon "',"
      "kor_modif='" prek/kor_modif "',dat_modif='" prek/dat_modif "' "
      "WHERE br_ugov='" prek/br_ugov "' AND serija='" prek/serija "'"
    ]
    if nab
    [
      mysql_cmd compose
      [
        "UPDATE nabavka AS t1 "
        "SET t1.plandat_isp=" apjel (zero_datum) apjel " "
        "WHERE t1.br_dok IN (" (mylist_form (extract prek/podaci_nab 2)) ")"
      ]
      blk0: make block! []
      mysql_cmd
      [
        "SELECT t1.sif_robe AS kulcs,t1.br_ugov,t1.serija,t1.mbr,t1.red_br,t1.sif_robe,t1.naz_robe,t1.jed_mere,"
        "t1.kol,t1.pot_kol,t1.cena,t1.tip,t1.naziv2,t1.opis,t1.sifra_nar,t1.sif_proizv,t1.naz_proizv,t1.tarif_car,t1.valuta,"
        "t1.dok_zahteva,t1.mbr_zahteva,t1.dok_nabavke,t1.mbr_nabavke,t1.kor_insert,t1.dat_insert,'0' AS izbor,"
        "IFNULL((SELECT SUM(t2.pristiglo) FROM prijem AS t2 WHERE t2.dok_nab=t1.dok_nabavke AND t2.mbr_nab=t1.mbr_nabavke),0) AS pristiglo "
        "FROM prom_pur AS t1 "
        "WHERE t1.br_ugov='" prek/br_ugov "' AND t1.serija='" prek/serija "' AND t1.proforma='' "
        "ORDER BY t1.red_br ASC"
      ]
      ptar: get_rekordset/index copy db
      foreach [ sif0 row0 ] ptar
      [
        append blk0 (rejoin [ row0/dok_nabavke "|" row0/mbr_nabavke ])
      ]
      mysql_cmd compose
      [
        "UPDATE nabavka AS t1 "
        "SET t1.plandat_isp=" apjel (prek/plandat_isp) apjel " "
        "WHERE CONCAT(t1.br_dok,'|',t1.mbr) IN (" (mylist_form blk0) ")"
      ]
    ]
  ]
  
  ;-- ez majd nem kell
  set 'promet_korpa: func [ /local p1 vred blk_nar nab_tar row0 tar0 sif1 row1 sz0 c0 c2 poz1 ]
  [
    blk_nar: make block! []
    ;
    mysql_cmd
    [
      "SELECT t1.sif_robe AS kulcs,t1.br_ugov,t1.serija,t1.mbr,t1.red_br,t1.sif_robe,t1.naz_robe,t1.jed_mere,"
      "t1.kol,t1.pot_kol,t1.cena,t1.tip,t1.naziv2,t1.opis,t1.sifra_nar,t1.sif_proizv,t1.naz_proizv,t1.tarif_car,t1.valuta,"
      "t1.dok_zahteva,t1.mbr_zahteva,t1.dok_nabavke,t1.mbr_nabavke,t1.kor_insert,t1.dat_insert,'0' AS izbor,"
      "IFNULL((SELECT SUM(t2.pristiglo) FROM prijem AS t2 WHERE t2.dok_nab=t1.dok_nabavke AND t2.mbr_nab=t1.mbr_nabavke),0) AS pristiglo "
      "FROM prom_pur AS t1 "
      "WHERE t1.br_ugov='" prek/br_ugov "' AND t1.serija='" prek/serija "' AND t1.proforma='' "
      "ORDER BY t1.red_br ASC"
    ]
    ptar: get_rekordset/index copy db
    ;
    p1: -1x-1
    tabla_blk: make block! compose
    [
      size p1
      style tlab text with [ edge: [color: coal size: 1x1 ] font: [ space: 0x-2 ] para: [ origin: 1x4 ] ]
      style t_chk check with [ edge: [ color: coal size: 1x1 ] ]
      backdrop color_g4
      across space -1x8
      at (p1) 
      tlab center "" 36x40 color_h1 color_h2
      tlab center "Red br." 40x40 color_h1 color_h2
      tlab center as-is "Šifra naruèiv."      90x40 color_h1 color_h2
      tlab center "Naziv naruèivanja"        280x40 color_h1 color_h2
      tlab center as-is "Jed.^/mere"          50x40 color_h1 color_h2
      tlab center as-is "Potrebna^/kolièina"  80x40 color_h1 color_h2
      tlab center as-is "Stvarna^/kolièina"   80x40 color_h1 color_h2
      tlab center as-is "Primljena^/kolièina" 80x40 color_h1 color_h2
      tlab center as-is ("Nabav.^/cena")      90x40 color_h1 color_h2
      tlab center "Nabav.^/vrednost"         100x40 color_h1 color_h2
      tlab center as-is "Val^/uta"            50x40 color_h1 color_h2
      tlab center "Tarifna oznaka"           150x40 color_h1 color_h2
      tlab center "Oznaka proizvoda"         170x40 color_h1 color_h2
    ]
    p1: p1 + 0x39
    c0: color_t6
    c2: color_t2
    poz1: head ptar
    while [ not tail? poz1 ]
    [
      row1: poz1/2
      ;
      vred: (dec_value row1/kol) * (dec_value row1/cena)
      ;
      append tabla_blk compose/only
      [
        at (p1 + 5x7) t_chk 24x24 with (compose/only [ user-data: (row1) ] )
        [
          row1: face/user-data
          either face/data [ row1/izbor: "1" ][ row1/izbor: "0" ]
        ]
        at (p1 + 35x0)
        tlab center (row1/red_br) 40x40 color_t1 color_t2
      ]
      ;
      if sz0 <> row1/naz_proizv
      [
        either c0 = color_t2 [ c2: color_t6 ][ c2: color_t2 ]
      ]
      append tabla_blk compose/only
      [
        tlab center (row1/sif_robe)           90x40 color_b1 color_b2
        tlab left   (row1/naz_robe)          280x40 color_t1 (c2)
        tlab center (row1/jed_mere)           50x40 color_t1 (c2)
        tlab center (row1/pot_kol)            80x40 color_t1 (c2)
        tlab center (row1/kol)                80x40 color_t1 (c2)
        tlab center (row1/pristiglo)          80x40 color_t1 (c2)
        tlab right  (dec_form row1/cena 0 2)  90x40 color_t1 (c2)
        tlab right  (dec_form vred 0 2)      100x40 color_t1 (c2)
        tlab center (row1/valuta)             50x40 color_t1 (c2)
        tlab center (row1/tarif_car)         150x40 color_t1 (c2)
        tlab left   (row1/naz_proizv)        170x40 color_t1 (c2)
      ]
      p1: p1 + 0x39
      ;
      sz0: copy row1/naz_proizv
      c0: c2
      poz1: next next poz1
    ]
    ;
    p1/2: max (p1/2 + 50) ablak_d/size/2
    p1: max (p1 + 1800x20) ablak_d/size
    ablak_d/pane: layout tabla_blk
    ablak_d/user-data: min max ablak_d/user-data (0x0 - p1) 0x0
    ablak_d/pane/offset: ablak_d/user-data
    show ablak_d
  ]
  
  ablak_zahtev: func [ /local p1 poz1 ]
  [
    c1: color_b2
    tabla_blk: compose
    [
      size p1
      backtile polished color_g4
      style tlab text middle with [ edge: [color: coal size: 1x1 ]]
      across space -1x1
    ]
    p1: 5x5
    poz1: head prek/lista_zaht
    while [ not tail? poz1 ]
    [
      append tabla_blk compose/only
      [
        at (p1) tlab center (poz1/1) 120x24 color_t1 color_b2
        [
          insert/only nyomtar reduce [ program_nev prek/br_ugov ]
          nyomelem: reduce [ face/text ]
          program_nev: ~"Zahtev-nabavke" program_indito
        ]
      ]
      
      p1: p1 + 125x0
      poz1: next poz1
    ]
    p1: max (p1 + 0x50) ablak_q2/size
    ablak_q2/pane: layout/offset tabla_blk 0x0
    show ablak_q2
  ]
  
  konacni_ablak: func [ p1[pair!] /local panel_blk panel_blk1 p2 poz0 row0 ]
  [
    p2: -1
    panel_blk1: compose/only
    [
      
      style tlab text with [ edge: [color: coal size: 1x1 ] ]
      style t_fld field 100x24
      across space -1x1
    ]
    either zar_jel1 = "-" [ c0: color_h4 ][ c0: color_h2 ]
    append panel_blk1 compose
    [
      at (as-pair -1 p2) box color_h2 (as-pair (meretx - meretb - 53) 28) with [ edge: [color: coal size: 1x1 ] ]
      at (as-pair -1 p2) tjel (zar_jel2) 28x28 color_h1 (c0)
      [
        either zar_jel2 = "+" [ zar_jel2: copy "-" ][ zar_jel2: copy "+" ]
      ]
      at (as-pair  35 p2) text middle "Konaèni podaci:" 500x28
      at (as-pair 285 p2) text right middle "Faza:" 70x28
      at (as-pair 355 p2) drop-down middle data faza_blk/2 120x28
      with [ rows: 4 text: ( blk_find faza_blk/1 faza_blk/2 prek/faza_kon) ]
      [
        if string? face/data
        [
          if not write_mode [ exit ]
          ;-- visszafele nem lehet allitani a fazist
          if face/text < prek/faza_kon
          [
            alert "Nije dozvoljeno!!!" exit
          ]
          prek/faza_kon: blk_find faza_blk/2 faza_blk/1 face/text
          pur_kiir
          pur_podaci
        ]
      ]
    ]
    p2: p2 + 27
    ;
    append panel_blk1 compose
    [
      at (as-pair  10 (p2 + 10)) text right "Broj faktura:"    100x24
      at (as-pair 110 (p2 + 10)) tlab center prek/br_ugov      130x24 color_t1 color_b2
      [
        insert/only nyomtar reduce [ program_nev prek/br_ugov ]
        nyomelem: reduce [ prek/br_ugov ]
        program_nev: ~"Ino-Faktura" program_indito
      ]
      at (as-pair 240 (p2 + 10)) text right "Dat.fakt.:"       100x24
      at (as-pair 340 (p2 + 10)) info center (to-reg-date prek/dat_fakt) 120x24 color_g2
      ;
      at (as-pair  10 (p2 + 40)) text right "Broj otprem.:"    100x24
      at (as-pair 105 (p2 + 40)) info center prek/sif_dok      130x24 color_g2
      at (as-pair 240 (p2 + 40)) text right "Dat.otprem.:"     100x24
      at (as-pair 340 (p2 + 40)) info center (to-reg-date prek/dat_fakt) 120x24 color_g2
      ;
      at (as-pair  10 (p2 + 75)) text right "Dok.prod:"        100x24
      at (as-pair 105 (p2 + 75)) info center prek/veza_dok     130x24 color_g2
      ;
      at (as-pair   0 (p2 + 115)) text right "Broj otpr. PUR:"  125x24
      at (as-pair 125 (p2 + 115)) info center prek/dok_otprem   110x24 color_g2
      ;
      at (as-pair   0 (p2 + 150)) text right "Dat. otpr. PUR:"  125x24
      at (as-pair 125 (p2 + 150)) w_date prek/dat_otprem        130x24 akcio []
      at (as-pair 260 (p2 + 150)) text right "Magacin:"         100x24
      at (as-pair 360 (p2 + 150)) info center prek/sif_mag       60x24 color_g2
      ;
      at (as-pair   0 (p2 + 185)) text right "Datum realiz.:"   125x24
      at (as-pair 125 (p2 + 185)) w_date prek/dat_istupa        130x24 akcio
      [
        prek/otp_kurs: kurs_valute_nadan/euro "EUR" prek/dat_istupa
        pur_kiir
        pur_podaci
      ]
      at (as-pair 260 (p2 + 185)) text right "Kurs otpr.:"      100x24
      at (as-pair 360 (p2 + 185)) info right (money_form prek/otp_kurs 4) 100x24 color_g2
      ;
      at (as-pair  10 (p2 + 220)) text "Opis otpremnice:" 300x24
      at (as-pair  10 (p2 + 245)) field prek/opis_otprem  450x48
      ;
      at (as-pair 485 (p2 + 10)) btn  "Ugovor - PUR (PDF)"    130x40 color_l2
      [
         stamp_pur_ugov_pdf prek/br_ugov prek/serija "Konaèni"
      ]
      at (as-pair 485 (p2 + 55)) btn "Konaèni normativ - Repro (PDF)"  130x40 color_l2
      [
        stamp_pur_kon_normativ_pdf prek/br_ugov prek/serija prek/lista_nar
      ]
      at (as-pair 485 (p2 + 100)) btn "Ugovor - PUR (XML)"    130x40 color_l6
      [
        stamp_pur_ugov_xml prek/br_ugov prek/serija "Konaèni"
      ]
      at (as-pair 485 (p2 + 145)) btn "Konaèni normativ - Repro (XML)" 130x40 color_l6
      [
        stamp_pur_kon_normativ_xml prek/br_ugov prek/serija prek/lista_nar
      ]
    ]
    either prek/dok_otprem = ""
    [
      append panel_blk1 compose
      [
        at (as-pair 250 (p2 + 115)) button "Kreir.otp"  100x24 color_m2
        [
          kreiranje_otpremnice_pur prek ptar
          pur_kiir
          ;
          ablak_arajz
          pur_podaci
        ]
      ]
    ]
    [
      append panel_blk1 compose
      [
        at (as-pair 250 (p2 + 115)) button "Otpremnica" 100x24 color_l2
        [
          if prek/dok_otprem <> ""
          [
            god1: copy/part prek/dat_otprem 4
            if god1 <> baze_sif [ alert "Datum nije iz ove godine!" exit ]
            insert/only nyomtar reduce [ program_nev prek/br_ugov ]
            nyomelem: reduce [ prek/dok_otprem ]
            program_nev: ~"Knjiženje-mag" program_indito
          ]
        ]
        at (as-pair 360 (p2 + 115)) button "Bris.otpr." 100x24 color_m4
        [
          if brisanje_otpremnice_pur prek
          [
            prek/dok_otprem: copy ""
            prek/dat_otprem: copy zero_datum
            prek/dat_istupa: copy ""
            prek/otp_kurs:   0.0
            ;
            pur_kiir
            ;
            ablak_arajz
            pur_podaci
          ]
        ]
      ]
    ]
    p2: p2 + 300
    panel_blk: compose
    [
      size (as-pair (meretx - meretb - 55) (p2 + 10))
      backtile polished color_g4
    ]
    append panel_blk compose/only panel_blk1
    append tabla_blk compose/only
    [
      at (p1)  panel with [ edge: [color: coal size: 1x1 ]] panel_blk
    ]
    return (p2 + 10)
  ]
  
  privremeni_ablak: func [ p1[pair!] /local panel_blk panel_blk1 p2 poz0 row0 ]
  [
    p2: -1
    panel_blk1: compose/only
    [
      style tlab text with [ edge: [color: coal size: 1x1 ] ]
      style t_fld field 100x24
      across space -1x1
    ]
    either zar_jel1 = "-" [ c0: color_h4 ][ c0: color_h2 ]
    append panel_blk1 compose
    [
      at (as-pair -1 p2) box color_h2 (as-pair (meretx - meretb - 53) 28) with [ edge: [color: coal size: 1x1 ] ]
      at (as-pair -1 p2) tjel (zar_jel1) 28x28 color_h1 (c0)
      [
        either zar_jel1 = "+" [ zar_jel1: copy "-" ][ zar_jel1: copy "+" ]
      ]
      at (as-pair  35 p2) text middle "Privremeni podaci:" 500x28
      at (as-pair 285 p2) text right middle "Faza:" 70x28
      at (as-pair 355 p2) drop-down middle data faza_blk/2 120x28
      with [ rows: 4 text: ( blk_find faza_blk/1 faza_blk/2 prek/faza_priv) ]
      [
        if string? face/data
        [
          if not write_mode [ exit ]
          ;-- visszafele nem lehet allitani a fazist
          if face/text < prek/faza_priv
          [
            alert "Nije dozvoljeno!!!" exit
          ]
          prek/faza_priv: blk_find faza_blk/2 faza_blk/1 face/text
          ;-- megkrealni a beszerzesi dokumentumot
          if prek/faza_priv = "P"
          [
            upis_nabav_dokum
          ]
          pur_kiir
          pur_adatok_olvas prek/br_ugov prek/serija
          ablak_arajz
          pur_podaci
        ]
      ]
    ]
    p2: p2 + 27
    ;
    ;-- LISTA ZAHTEVA -- kesobb majd megcsinalom, nem fontos resz
    ;append panel_blk1 compose
    ;[
    ;  at (as-pair 625 (p2 + 15)) box color_h2 200x28 with [ edge: [color: coal size: 1x1 ] ]
    ;  at (as-pair 625 (p2 + 15)) text "Lista zahteva:" 200x24 
    ;  at (as-pair 625 (p2 + 42)) ablak_q2: box color_g4 200x55 with [ edge: [ color: coal size: 1x1 ] ]
    ;  at (as-pair 625 (p2 + 42)) slider 320x20 with [ user-data: ablak_q2 ] [ scroller_pozicio face  ]
    ;]
    ;
    append panel_blk1 compose
    [
      ;at (as-pair 5 (p2 + 5)) text left "Podaci nabavke:" 200x24
      ;
      at (as-pair  10 (p2 + 10)) text right "Oèekiv.dat.isp.:" 130x24
      at (as-pair 140 (p2 + 10)) w_date prek/plandat_isp 130x24 akcio
      [
        pur_kiir/nab
      ]
      ;
      at (as-pair  10 (p2 + 40)) text  right  "Broj odobrenja:" 130x24
      at (as-pair 140 (p2 + 40)) field prek/br_odobr            130x24
      [
        pur_kiir
      ]
      at (as-pair   0 (p2 + 70)) text  right  "Datum izdavanje:" 140x24
      at (as-pair 140 (p2 + 70)) w_date prek/dat_odobr           130x24 akcio
      [
        pur_kiir
      ]
      at (as-pair 160 (p2 + 100)) button ~"Arhiva" 100x24 color_l2
      [
        ablak_p1rajz
      ]
      at (as-pair 270 (p2 + 100)) info center ""    60x24 color_g2
      ;
      at (as-pair 350 (p2 + 15)) btn "Ugovor - PUR (PDF)"   130x40 color_l2
      [
         stamp_pur_ugov_pdf prek/br_ugov prek/serija "Privremeni"
      ]
      at (as-pair 485 (p2 + 15)) btn "Privremeni kupljeni normativ (PDF)" 130x40 color_l2
      [
        stamp_pur_normativ_pdf prek/br_ugov prek/serija prek/lista_nar
      ]
      at (as-pair 350 (p2 + 65)) btn "Ugovor - PUR (XML)"   130x40 color_l6
      [
        stamp_pur_ugov_xml prek/br_ugov prek/serija "Privremeni"
      ]
      at (as-pair 485 (p2 + 65)) btn "Privremeni kupljeni normativ (XML)" 130x40 color_l6
      [
        stamp_pur_normativ_xml prek/br_ugov prek/serija prek/lista_nar
      ]
    ]
    p2: p2 + 130
    append panel_blk1 compose
    [
      at (as-pair  10 (p2 + 10))
      tlab center "Podaci nabavke:"  605x28 color_h1 color_h2
      at (as-pair  10 (p2 + 37))
      tlab center "Dok.nab."    141x28 color_h1 color_h2
      tlab center "Datum nab."  121x28 color_h1 color_h2
      tlab center as-is "Dok.prijema" 131x28 color_h1 color_h2
      tlab center as-is "Dat.prijema" 121x28 color_h1 color_h2
      tlab center "JCI broj"  95x28 color_h1 color_h2
    ]
    p2: p2 + 64
    foreach [ sif1 row1 ] prek/podaci_nab
    [
      c1: color_b2
      ;
      append panel_blk1 compose/only
      [
        at (as-pair 10 p2)
        tlab center (row1/br_nab) 141x28 color_b1 (c1)
        with (compose/only [ data: (row1) ])
        [
          row1: face/data
          sz2: rejoin [ " AND t1.tip_zaht=" apjel "P" apjel " AND t1.br_dok='" row1/br_nab "' " ]
          mysql_cmd compose
          [
            "SELECT t1.*,(SELECT COUNT(*) FROM prom_potnab AS t2 WHERE t2.dok_nabavke=t1.br_dok) AS sorok1,'0' AS rabat,'' AS plandat_isp "
            "FROM dok_nab AS t1 WHERE 1" (sz2)
            "ORDER BY br_dok ASC "
          ]
          link_rek: get_rekord first db
          plan_secenja link_rek none true
          ;
          insert/only nyomtar reduce [ program_nev prek/br_ugov ]
        ]
        tlab center (row1/dat_nab)  121x28 color_b1 color_t2
        tlab center (row1/br_prij)  131x28 color_t1 (c1)
        with (compose/only [ data: (row1) ])
        [
          row1: face/data
          if row1/br_prij = "" [ exit ]
          ;
          insert/only nyomtar reduce [ program_nev prek/br_ugov ]
          nyomelem: reduce [ row1/br_prij ]
          program_nev: ~"Prijem-robe"
          program_indito
        ]
        tlab center (row1/dat_prij) 121x28 color_b1 color_t2
        tlab center (row1/jci_prij)  95x28 color_t1 color_t2
      ]
      p2: p2 + 27
    ]
    panel_blk: compose
    [
      size (as-pair (meretx - meretb - 55) (p2 + 10))
      backtile polished color_g4
    ]
    append panel_blk compose/only panel_blk1
    append tabla_blk compose/only
    [
      at (p1)  panel with [ edge: [color: coal size: 1x1 ]] panel_blk
    ]
    return (p2 + 10)
  ]
  
  narudzba_ablak: func [ p1[pair!] /local panel_blk panel_blk1 p2 poz0 row0 ]
  [
    p2: -1
    panel_blk1: compose/only
    [
      style tlab text with [ edge: [ color: coal size: 1x1 ] ]
      style t_fld field 100x24
      across space -1x1
    ]
    append panel_blk1 compose/only
    [
      at (as-pair -1 p2) box color_h2 (as-pair (meretx - meretb - 53) 28) with [ edge: [color: coal size: 1x1 ] ]
      at (as-pair -1 p2) text "Lista narudžbe:" 500x24
      at (as-pair 464 p2) tlab center "Dodati narudžba" 150x28 color_b1 color_b2
      [
        alert mold 1
      ]
    ]
    p2: p2 + 35
    append panel_blk1 compose/only
    [
      at (as-pair 5 p2) 
      tlab center "Narudžba"      101x38 color_h1 color_h2
      tlab center as-is "Rok.isp." 61x38 color_h1 color_h2
      tlab center "Trans.dat"     101x38 color_h1 color_h2
      tlab center as-is "Naziv narudžba" 501x38 color_h1 color_h2
    ]
    p2: p2 + 37
    ;
    poz0: head pon_tar
    while [ not tail? poz0 ]
    [
      row0: poz0/1
      append panel_blk1 compose/only
      [
        at (as-pair 5 p2)
        tlab center (row0/sifra) 101x28 color_b1 color_b2
        with (compose [ user-data: (row0) ])
        [
          insert/only nyomtar reduce [ program_nev prek/br_ugov prek/serija ]
          nyomelem: reduce [ face/text ]
          program_nev: ~"Narudžba" program_indito
        ]
        tlab center (rejoin [ row0/rok_isp " kw" ]) 61x28 color_t1 color_t2
        tlab center (row0/dat_trans) 101x28 color_t1 color_t2
        t_fld (row0/naziv) 501x28
        with (compose [ user-data: (row0) ])
        [
          row0: face/user-data
          row0/naziv: copy face/text
          mysql_cmd compose
          [
            "UPDATE ponuda AS t1 SET t1.naziv='" row0/naziv "' WHERE t1.sifra='" row0/sifra "' "
          ]
        ]
      ]
      p2: p2 + 27
      poz0: next poz0
    ]
    panel_blk: compose
    [
      size (as-pair (meretx - meretb - 55) (p2 + 10))
      backtile polished color_g4
    ]
    append panel_blk compose/only panel_blk1
    append tabla_blk compose/only
    [
      at (p1)  panel with [ edge: [color: coal size: 1x1 ]] panel_blk
    ]
    return (p2 + 10)
  ]
  
  partner_ablak: func [ p1[pair!] /local panel_blk ]
  [
    panel_blk: compose/only
    [
      size (as-pair (meretx - meretb - 55) 155) across
      backtile polished color_g4
      style tlab text with [ edge: [color: coal size: 1x1 ] ]
    ]
    append panel_blk compose
    [
      at 5x5 button "Partner" 70x24 color_l2
      [
        insert/only nyomtar reduce [ program_nev prek/br_ugov ]
        nyomelem: reduce [ prek/sif_part ]
        program_nev: ~"Partner" program_indito
      ]
      at 80x5 arrow right 24x24 color_l2
      [
        if none? prek/sif_part [ prek/sif_part: copy "" ]
        knjig_partner_izbor prek/sif_part none none
        if object? retval
        [
          prek/sif_part:  copy retval/sifra
          prek/naz_part:  copy retval/name1
          prek/mesto:     rejoin [ retval/mesto " " retval/drzava ]
          prek/adresa:    copy retval/ulica
          prek/telefon:   copy retval/telefon
          prek/fax:       copy retval/fax
          prek/pib:       copy retval/pib
          prek/email:     copy retval/email
        ]
        pur_kiir
        pur_podaci
      ]
      at 110x5   info center (prek/sif_part)   100x24 color_g2
      at 220x5   info wrap   (prek/naz_part)   394x55 color_g2
      ;
      at   5x35  text right "PIB:"    75x24 at  80x35  info prek/pib       130x24 color_g2
      at   5x65  text right "Mesto:"  75x24 at  80x65  info prek/mesto     180x24 color_g2
      at 250x65  text right "Ulica:"  70x24 at 320x65  info prek/adresa    294x24 color_g2
      at   5x95  text right "Tel:"    75x24 at  80x95  info prek/telefon   180x24 color_g2        
      at 250x95  text right "Fax:"    70x24 at 320x95  info prek/fax       294x24 color_g2
      at   5x125 text right "Email:"  75x24 at  80x125 info prek/email     260x24 color_g2
      at 340x125 text right "Oznaka:" 80x24 at 420x125 info prek/ozn_kupca  30x24 color_g2
    ]
    append tabla_blk compose
    [
      at (p1)  panel with [ edge: [color: coal size: 1x1 ]] panel_blk
    ]
  ]
  
  set 'pur_podaci: func [ /local p1 dat1 c0 ]
  [
    zar_jel1: copy "-"
    zar_jel2: copy "-"
    ;if not none? prek/nab_dok [ pl_dok: copy prek/nab_dok ]
    p1: -1x-1
    tabla_blk: compose/only
    [
      size p1
      backdrop color_g4
      style tlab  text with [ edge: [color: coal size: 1x1 ] ]
      style tjel  label center with [ edge: [color: coal size: 1x1 ] ]
      across -1x-1
      ;
      at 9x9 button close_btn_img 26x26
      [
        ablak_cezar
        ablak_brajz
      ]
      at  40x9 text right "Broj ugovora:"  120x24
      at 160x9 info center (prek/br_ugov)  120x24 color_g2
      at 300x9 button "Promet" 120x24 color_b2
      [
        prom_pur prek
      ]
      at 450x9 button "Brisati PUR" 120x24 color_m4
      [
        if not find [ "adm" "sis" "prg" ] dozvola [ alert "Nije dozvoljeno!" exit ]
        if (request/confirm rejoin [ "Brisati kontrolnik " prek/br_ugov "?" ]) <> true [ exit ]
        mysql_cmd
        [
          "DELETE FROM evid_pur WHERE br_ugov='" prek/br_ugov "' AND serija='" prek/serija "'"
        ]
        mysql_cmd
        [
          "DELETE FROM prom_pur WHERE br_ugov='" prek/br_ugov "' AND serija='" prek/serija "' AND proforma=''"
        ]
        ;mysql_cmd
        ;[
        ;  "DELETE FROM faktura2 WHERE br_fak='" prek/br_ugov "'"
        ;]
        mysql_cmd
        [
          "UPDATE dok_nab AS t1 SET t1.dok_dobav='' WHERE t1.dok_dobav='" prek/br_ugov "'"
        ]
        evid_pur
      ]
    ]
    if korisnik = "csa"
    [
      append tabla_blk compose
      [
        at 600x9 button "Osvezi" 120x24 color_b2
        [
          promet_betolt_nov
        ]
      ]
    ]
    ;-- PARTNER ADATOK
    p1: p1 + 5x45
    partner_ablak p1
    ;-- LISTA NARUDZBE
    p1: 5x210
    p1: p1 + (as-pair 0 (narudzba_ablak p1) + 10)
    ;-- PRIVREMENI RESZ
    p1: p1 + (as-pair 0 (privremeni_ablak p1) + 10)
    ;-- KONACNI RESZ
    p1: p1 + (as-pair 0 (konacni_ablak    p1) + 10)
    ;
    p1: max (p1 + 0x20) ablak_c/size
    ablak_c/pane: layout/offset tabla_blk ablak_c/user-data
    show ablak_c
  ]
  
  promet_betolt_nov: func [ /local poz0 tar0 row0 ]
  [
    ;-- felre teszem egy taroloba a prom_pur sorokat 
    if not none? ptar
    [
      btar: copy/deep ptar
      sort/skip btar 2
    ]
    ;-- PU-bol kiszedem a PUR-s cuccokat
    mysql_cmd compose
    [
      "SELECT CONCAT(t3.rbr,'|',(t2.dim * -1),'|',t2.rbr,'|',t1.red_br) AS kulcs,"
      "t1.sifra_nar,t1.sas_vek,t1.mbr,t1.sif_proizv,t1.red_br,t1.sif_mat,t1.naziv,(t1.kol * t2.pot_kol) AS pot_kol,"
      "t1.tip,t3.sif_proizv AS visa_sif,t3.rbr AS visa_rbr,t3.naziv AS visa_naz,"
      "IF(t1.jed_mere='',IFNULL((SELECT t4.jed_mere FROM materijal AS t4 WHERE t4.sif_mat=t1.sif_mat0),''),t1.jed_mere) AS jed_mere "
      "FROM sastav_mat AS t1 "
      "LEFT JOIN sastav_nar AS t2 ON t2.sifra_nar=t1.sifra_nar AND t2.sas_vek=t1.sas_vek "
      "LEFT JOIN sastav_nar AS t3 ON t3.sifra_nar=t1.sifra_nar "
      "AND t3.sas_vek=IF(LOCATE('.',t1.sas_vek),MID(t1.sas_vek,1,LOCATE('.',t1.sas_vek)-1),t1.sas_vek) "
      "WHERE t1.sifra_nar='IZ-9315' AND t1.alt LIKE 'P%' "
      "ORDER BY t3.rbr ASC,t2.dim DESC,t2.rbr ASC,t1.red_br ASC"
    ]
    tar0: get_rekordset copy db
    poz0: head tar0
    while [ not tail? poz0 ]
    [
      row0: poz0/2
      ;-- osszerakom a PU poreklot
      por_nar: rejoin [ row0/sifra_nar "|" row0/sas_vek "|" row0/mbr "|" row0/sif_proizv ]
      
      alert mold por_nar
      
      
      poz0: next next poz0
    ]
    
    
    
  ]
  
  promet_betolt: func [ /local dat0 blk0 blk1 blk2 tar0 tar1 tar2 tar3 tar4 btar poz0 poz1 poz2 poz3 row0 row1 row2 row3 sz1 sz2 i1 i2 i3 n1 n2 n3 
                        filt1 ]
  [
    dat0: sistime/iso
    blk0: make block! []
    blk1: make block! []
    blk2: make block! []
    tar3: make block! []
    tar4: make block! []
    btar: make block! []
    blk_naslov: make block! []
    ;-- felre teszem egy taroloba a prom_pur sorokat 
    if not none? ptar
    [
      btar: copy/deep ptar
      sort/skip btar 2
    ]
    ;-- torlom a tablat
    mysql_cmd [ "DELETE FROM prom_pur WHERE br_ugov='" prek/br_ugov "' AND serija='" prek/br_ugov "' AND proforma=''" ]
    ;
    mysql_cmd compose
    [
      "SELECT t1.br_dok,t1.mbr,t1.red_br,t1.sif_robe,t1.naz_robe,t1.naruc,t1.jed_mere,t1.tip_robe,t1.jed_cena,t1.tarif_car,"
      "t2.br_dok AS dok_zaht,t2.mbr AS mbr_zaht,t2.sifra_nar,t2.sif_proizv,'' AS sas_rbr,'' AS naz_proizv,'' AS lista_svek,'' AS lista_svek2,"
      "IFNULL((SELECT GROUP_CONCAT(t3.sas_vek ORDER BY t3.sas_vek ASC SEPARATOR ' ') FROM sastav_nar AS t3 "
              "WHERE t3.sifra_nar=t2.sifra_nar AND t3.sif_proizv=t2.sif_proizv),'') AS svek,"
      "IFNULL((SELECT t4.valuta FROM dok_nab AS t4 WHERE t4.br_dok=t1.br_dok),'') AS dok_val,0 AS kol "
      "FROM nabavka AS t1 "
      "LEFT JOIN prom_potnab AS t2 ON t2.dok_nabavke=t1.br_dok AND t2.mbr_nabavke=t1.mbr "
      "WHERE t1.br_dok IN (" (mylist_form prek/dok_nabavke) ") "
      "GROUP BY t1.br_dok,t1.mbr ORDER BY t1.red_br ASC"
    ]
    tar0: get_rekordset copy db
    ;
    poz0: head tar0
    while [ not tail? poz0 ]
    [
      row0: poz0/1
      row0/lista_svek:  make block! []
      row0/lista_svek2: make block! []
      ;
      append blk0 row0/sifra_nar
      ;
      blk1: parse row0/svek none
      row0/lista_svek: blk1
      i1: 0   n1: length? blk1
      row0/lista_svek2: array n1
      for i1 1 (length? blk1) 1
      [
        row0/lista_svek2/(i1): make block! []
      ]
      ;
      poz1: head blk1
      while [ not tail? poz1 ]
      [
        i1: i1 + 1
        i2: 0   n2: length? poz1/1
        for i2 1 n2 1
        [
          sz1: copy/part poz1/1 i2
          if (last sz1) <> #"."
          [
            append row0/lista_svek2/(i1) (rejoin [ row0/sifra_nar "|" sz1 ])
            append blk2 (rejoin [ row0/sifra_nar "|" sz1 ])
          ]
        ]
        poz1: next poz1
      ]
      poz0: next poz0
    ]
    blk0: unique blk0
    sort/skip blk0 1
    blk2: unique blk2
    sort/skip blk2 1
    ;-- kiszedem a PU-bol a PUR-s anyagokat
    mysql_cmd compose
    [
      "SELECT CONCAT(t1.sifra_nar," apjel "|" apjel ",t1.sas_vek," apjel "|" apjel ",t2.sif_mat) AS kulcs,"
      "t1.sifra_nar,t1.sas_vek,t1.sif_proizv,t2.sif_mat,t1.pot_kol,t2.kol "
      "FROM sastav_nar AS t1 "
      "LEFT JOIN sastav_mat AS t2 ON t2.sifra_nar=t1.sifra_nar AND t2.sas_vek=t1.sas_vek "
      "WHERE t1.sifra_nar IN (" (mylist_form blk0) ") AND t2.alt=" apjel "P" apjel
    ]
    tar1: get_rekordset/index copy db
    sort/skip tar1 2
    ;-- kiszedem a PU-bol az eladasi szinteket (ez a default, de lehet majd megrendelo szint vagy paket szint, ha van)
    mysql_cmd compose
    [
      "SELECT CONCAT(t1.sifra_nar," apjel "|" apjel ",t1.sas_vek) AS kulcs,t1.sifra_nar,t1.sif_proizv,t1.naziv,t1.rbr,t1.pred_kol,t1.pot_kol "
      "FROM sastav_nar AS t1 WHERE CONCAT(t1.sifra_nar,'|',t1.sas_vek) IN (" (mylist_form blk2) ") AND t1.pred=" apjel "P" apjel
    ]
    tar2: get_rekordset/index copy db
    sort/skip tar2 2
    ;
    blk0: make block! []
    sz1:  copy ""
    mysql_bulk: copy ""
    foreach row0 tar0
    [
      if error? try
      [
        sz_proizv: copy row0/sif_proizv
      ][]
      ;row0/sif_robe: copy next find row0/sif_robe "-"
      poz0: head row0/lista_svek
      while [ not tail? poz0 ]
      [
        sz1: rejoin [ row0/sifra_nar "|" poz0/1 "|" row0/sif_robe ]
        ;
        poz1: index_block_find tar1 sz1 2
        if poz1/1 = sz1
        [
          row1: poz1/2
          row0/naruc: (int_value row1/pot_kol) * (dec_value row1/kol)
        ]
        row0/kol: row0/naruc
        row0/sif_proizv: copy ""
        sz2: copy ""
        poz2: head row0/lista_svek2/(index? poz0)
        while [ not tail? poz2 ]
        [
          poz3: index_block_find tar2 poz2/1 2
          if poz3/1 = poz2/1
          [
            row3: poz3/2
            if row0/sif_proizv = ""
            [
              row0/sif_proizv: copy row3/sif_proizv
              row0/sas_rbr:    copy row3/rbr
              row0/naz_proizv: copy row3/naziv
            ]
            append blk_naslov (rejoin [ row3/sifra_nar "|" row3/rbr "|" row3/naziv "|" row3/pred_kol "|" "UGOVORU O SKLAPANJU SKLOPA ZA " ])
          ]
          poz2: next poz2
        ]
        ;
        poz0: next poz0
      ]
      append tar4 (rejoin [ row0/sifra_nar "|" row0/sas_rbr "|" row0/sif_robe ])
      append tar4 row0
    ]
    ;
    sort/skip tar4 2
    i1: 0
    poz0: head tar4
    while [ not tail? poz0 ]
    [
      row4: poz0/2
      i1: i1 + 1
      ;-- kikeresem a backup-bol a materijal cenat es a nazivot
      poz1: index_block_find btar row4/sif_robe 2
      if poz1/1 = row4/sif_robe
      [
        row4/kol:      (dec_value poz1/2/kol)
        row4/jed_cena:  copy poz1/2/cena
        row4/naz_robe:  copy poz1/2/naz_robe
        row4/tarif_car: copy poz1/2/tarif_car
      ]
      ;
      if mysql_bulk <> "" [ append mysql_bulk "," ]
      append mysql_bulk rejoin
      [
        "('" prek/br_ugov "','" prek/serija "','" i1 "','','" row4/sif_robe "','" row4/naz_robe "','" row4/jed_mere "','" 
        (int_value row4/kol) "','" (int_value row4/naruc) "','" (dec_value row4/jed_cena) "','" row4/tip_robe "',"
        "'','','" row4/dok_val "','" row4/sifra_nar "','" row4/sas_rbr "','" row4/sif_proizv "','" row4/naz_proizv "','" 
        row4/tarif_car "','" row4/dok_zaht "','" row4/mbr_zaht "','" row4/br_dok "','" row4/mbr "','" korisnik "','" dat0 "')"
      ]
      poz0: next next poz0
    ]
    if mysql_bulk <> ""
    [
      mysql_cmd compose
      [
        "INSERT INTO prom_pur (br_ugov,serija,red_br,proforma,sif_robe,naz_robe,jed_mere,kol,pot_kol,cena,tip,naziv2,opis,valuta,sifra_nar,sas_rbr,"
        "sif_proizv,naz_proizv,tarif_car,dok_zahteva,mbr_zahteva,dok_nabavke,mbr_nabavke,kor_insert,dat_insert) VALUES " 
        (mysql_bulk)
      ]
    ]
    blk_naslov: unique blk_naslov
    sort/skip blk_naslov 1
    mysql_cmd compose
    [
      "UPDATE evid_pur SET naslov='" (blokk_pakolo blk_naslov "^/") "' "
      "WHERE br_ugov='" prek/br_ugov "'"
    ]
  ]
  
  set 'pur_adatok_olvas: func [ br_ugov[string!] serija[string!] /nov /local blk1 blk2 blk3 row0 tar1 row1 dat2 ]
  [
    mysql_cmd compose
    [
      "SELECT t1.br_ugov,t1.serija,t1.godina,t1.dat_ugov,t1.faza,t1.sif_part,t1.vrsta,t1.valuta,t1.sif_mag,t1.dok_nabavke,t1.br_odobr,t1.dat_odobr,"
      "t1.br_fak,t1.dok_otprem,t1.dat_otprem,t1.otp_valuta,t1.opis_otprem,t1.dat_istupa,t1.otp_kurs,t1.lista_nar,t1.plandat_isp,"
      "t1.kor_insert,t1.dat_insert,t1.kor_modif,t1.dat_modif," zero_datum " AS nab_dat,t1.faza_priv,t1.faza_kon,"
      "'' AS lista_zaht,'' AS lista_nar,t1.lista_nar AS lista_nar1,'' AS podaci_nab,'' AS sif_mag,'' AS prij_dok,"
      "'' AS prij_dat,'' AS jci_broj,'' AS br_fak,'' AS sif_dok,'' AS dat_fakt,'' AS veza_dok,'' AS primalac,'' AS sifra_nar,"
      "t2.name1 AS naz_part,t2.ozn_kupca,t2.mesto,t2.ulica AS adresa,t2.fax,t2.telefon,t2.pib,t2.email,t2.tip_p,"
      "IFNULL((SELECT GROUP_CONCAT(t10.br_dok ORDER BY t10.br_dok ASC SEPARATOR ' ') "
              "FROM dok_nab AS t10 WHERE t10.dok_dobav=t1.br_ugov),'') AS pod_nab,'' AS pod_prij,"
      "(SELECT CONCAT(t6.br_fak,'|',t6.sif_dok,'|',t6.date,'|',t6.veza_dok) FROM faktura2 AS t6 WHERE t6.br_fak=t1.br_ugov) AS pod_fakt,"
      "IFNULL((SELECT COUNT(*) FROM prom_pur AS t9 WHERE t9.br_ugov=t1.br_ugov AND t9.serija=t1.serija AND t9.proforma=''),'') AS sorok "
      "FROM evid_pur AS t1 "
      "LEFT JOIN " firma/data ".partners AS t2 ON t2.sifra=t1.sif_part "
      "WHERE t1.br_ugov=" apjel (br_ugov) apjel " AND t1.serija=" apjel (serija) apjel
    ]
    prek: get_rekord first db
    if not retval [ return false ]
    ;
    prek/lista_zaht: make block! []
    prek/podaci_nab: make block! []
    prek/sif_mag:    copy "30"
    prek/prij_dok:   copy ""
    prek/jci_broj:   copy ""
    prek/br_fak:     copy ""
    prek/sif_dok:    copy ""
    prek/dat_fakt:   copy ""
    prek/veza_dok:   copy ""
    ;
    if prek/dat_otprem  = zero_datum [ prek/dat_otprem: copy dat0    ]
    if prek/primalac    = ""  [ prek/primalac:    copy prek/sif_part ]
    if none? prek/opis_otprem [ prek/opis_otprem: copy "" ]
    if none? prek/pod_prij    [ prek/pod_prij: copy "" ]
    if none? prek/pod_fakt    [ prek/pod_fakt: copy "" ]
    ;
    prek/pod_nab: parse prek/pod_nab none
    prek/dok_nabavke: parse prek/dok_nabavke none
    ;
    if prek/opis_otprem = ""  [ prek/opis_otprem: rejoin [ "VEZA: PUR " prek/br_ugov ] ]
    ;
    ;-- kiszedem a narudzba adatait
    prek/lista_nar: parse prek/lista_nar1 none
    mysql_cmd compose
    [
      "SELECT t1.sifra,WEEK(t1.rok_isp,1) AS rok_isp,t1.dat_trans,t1.naziv "
      "FROM ponuda AS t1 "
      "WHERE t1.sifra IN (" (mylist_form prek/lista_nar) ") "
      "ORDER BY t1.sifra ASC"
    ]
    pon_tar: get_rekordset copy db
    ;
    ;-- kiszedem a nabavka adatait
    mysql_cmd compose
    [
      "SELECT DISTINCT t1.br_dok AS br_nab,t1.datum_dok,t2.plandat_isp,IFNULL(t3.br_dok," apjel "" apjel ") AS br_zaht,"
      "IFNULL(t4.br_dok," apjel "" apjel ") AS br_prij,IFNULL(t4.datum_dok," apjel "" apjel ") AS dat_prij,"
      "IFNULL((SELECT t5.jci_broj FROM dok_prij AS t5 WHERE t5.br_dok=t4.br_dok LIMIT 1)," apjel "" apjel ") AS jci "
      "FROM dok_nab AS t1 "
      "LEFT JOIN nabavka AS t2 ON t2.br_dok=t1.br_dok "
      "LEFT JOIN prom_potnab AS t3 ON t3.dok_nabavke=t2.br_dok AND t3.mbr_nabavke=t2.mbr "
      "LEFT JOIN prijem AS t4 ON t4.dok_nab=t2.br_dok AND t4.mbr_nab=t2.mbr "
      "WHERE t1.br_dok IN (" (mylist_form prek/dok_nabavke) ") "
      "ORDER BY t1.br_dok ASC"
    ]
    tar1: get_rekordset copy db
    ;
    sz0:  copy ""
    dat2: dat0
    foreach row1 tar1
    [
      append prek/lista_zaht copy row1/br_zaht
      if sz0 <> row1/br_nab
      [
        if dat2 = zero_datum [ dat2: prek/plandat_isp ]
        dat2: min dat2 row1/plandat_isp
        ;
        append prek/podaci_nab row1/br_nab
        append prek/podaci_nab make object!
        [
          br_nab:   (copy row1/br_nab)
          dat_nab:  (copy row1/datum_dok)
          plan_isp: (copy row1/plandat_isp)
          br_prij:  (copy row1/br_prij)
          dat_prij: (copy row1/dat_prij)
          jci_prij: (copy row1/jci)
        ]
      ]
      sz0: copy row1/br_nab
    ]
    prek/plandat_isp: copy dat2
    prek/lista_zaht: unique prek/lista_zaht
    sort/skip prek/lista_zaht 1
    ;
    blk2: parse/all prek/pod_fakt "|"
    ;
    if block? blk2
    [
      while [ (length? blk2) < 4 ]
      [
        append blk2 copy ""
      ]
      ;
      prek/br_fak:     copy blk2/1
      prek/sif_dok:    copy blk2/2
      prek/dat_fakt:   copy blk2/3
      prek/veza_dok:   copy blk2/4
    ]
    ;
    ;prek/lista_nar: copy prek/lista_nar1
    if prek/pod_nab <> prek/dok_nabavke
    [
      prek/dok_nabavke: copy prek/pod_nab
      pur_kiir
    ]
    ;prek/lista_nar:  make block! []
    ;if empty? prek/lista_nar [ prek/lista_nar: parse prek/lista_nar1 none ]
    if (int_value prek/sorok) = 0 [ promet_betolt ]
    return true
  ]
  
  ablak_cezar: func [ /local sz0 sz1 row1 row2 novi_sif blk0 ]
  [
    tabla_blk: compose
    [
      size ablak_c/size
      backtile polished color_f4
      across
      ;
      at 10x10 btn "Sz:M" 40x24 color_l2
      [
        set_window_size 'm
        ablak_arajz
      ]
      ;
      at  10x110 text right "Upis novog kontrolnika PURa:" 240x24
      at 260x110 button "Upis novog" 100x24 color_b2
      [
        if none? row1: oblik_dokum_rekord "FAK" "R" [ alert "Tip fakture nije dobar!" exit ]
        sz0: dokumentum_szam_keszito row1/dok_form2 baze_sif "faktura2" "br_fak"
        ;
        if string? sz0 [ novi_sif: copy sz0 ]
        if (length? novi_sif) < 4 [ alert "Broj dokumenta nije odreðen!" exit ]
        mysql_cmd [ "SELECT br_fak FROM faktura2 WHERE br_fak = '" novi_sif "'" ]
        if (not empty? first db)
        [
          alert rejoin [ ~"Faktura:" " '" novi_sif "' " ~"veæ postoji!" ] exit
        ]
        br_ugov: copy novi_sif
        ;
        sz1: request-text/title/default "Broj dokumenta:" br_ugov
        if not string? sz1 [ exit ]
        ser0: 0
        mysql_cmd [ "SELECT MAX(t1.serija) AS br_ser FROM evid_pur AS t1 WHERE t1.br_ugov='" sz1 "'" ]
        row2: get_rekord first db
        if retval
        [
          either none? row2/br_ser [ ser0: 0 ]
          [
            ser0: (int_value row2/br_ser) + 1
            if (request/confirm rejoin [ "Kontrolnik: " novi_sif " veæ postoji! Kreirati serija: " ser0 " ?" ]) <> true [ exit ]
          ]
        ]
        mysql_cmd
        [
          "INSERT IGNORE INTO evid_pur SET br_ugov='" sz1 "',serija='" ser0 "',vrsta='INO',valuta='EUR',"
          "sif_mag='30',dat_ugov=CURDATE(),godina='" baze_sif "',"
          "kor_insert='" korisnik "',dat_insert=CURDATE(),kor_modif='" korisnik "',dat_modif=CURDATE() "
        ]
        mysql_cmd
        [
          "INSERT IGNORE INTO faktura2 SET br_fak='" br_ugov "',otp_valuta='EUR',otp_kurs='" firma_rek/kurs_euro "',"
          "best_nr='da',best_bk='ne',po_nar='da',zaglav='da',opis='',kor_insert='" korisnik "',dat_insert=CURDATE() "
        ]
        ;
        ;-- kivalasztom a nabavkas korpat
        blk0: pur_korpa_keres br_ugov (to-string ser0)
        ;-- beolvasom
        if not pur_adatok_olvas/nov br_ugov (to-string ser0)
        [
          prek: none
          alert rejoin [ "Dok.prodaje " br_ugov " nije u bazi!" ]
          ablak_brajz
          exit
        ]
        pur_kiir
        ;
        ablak_arajz
        pur_podaci
      ]
    ]
    ablak_c/pane: layout/offset tabla_blk 0x0
    show ablak_c 
  ]
  
  ablak_brajz: func [ /local ]
  [
    tabla_keres "evid_pur"
    tabla/fejlec: ~"Izbor evidencija PUR"
    tabla/name: rejoin
    [
      "evid_pur as t1 LEFT JOIN " firma/data ".partners AS t2 ON t2.sifra=t1.sif_part "
    ]
    tabla/lista:  rejoin
    [ 
      "t1.br_ugov|t1.serija|t1.dat_ugov|t1.sif_part|t2.name1|t2.mesto|t2.ulica|t1.lista_nar"
    ]
    tabla/nevek: rejoin
    [
       ~"Broj kontrolnika|Serija|Datum|Sif. partn.|Naziv partnera|Mesto|Ulica|Lista narudžbe"
    ]
    tabla/oszlop: [ 120   50  100   70  150 150 150 150 ]
    tabla/tipus:  [ "CS" "CS" "CD" "CS" ""  ""  ""  ""  ]
    tabla/order:  [ 1 "DESC" 2 "DESC" ]
    tabla/index:  [ 1 2 ]
    tabla/filter: rejoin [ "t1.godina=" apjel (god0) apjel ]
    ;
    tabla/pane: compose
    [
      at 320x3 text right "Godina:" 100x24
      at 420x3 drop-down 80x24 data god_list with [ rows: 5 text: god0 ]
      [
        if string? face/data
        [
          god0: copy face/text
          ablak_brajz
        ]
      ]
    ]
    tabla/akcio: func [ adat ]
    [
      pur_adatok_olvas adat/1 adat/2
      pur_podaci
      ;
      tabla/value: none
      if object? prek [ tabla/value: reduce [ prek/br_ugov prek/serija ] ]
      tabla/ablak:  ablak_b
      tabla_view
    ]
    tabla/value: none
    if object? prek [ tabla/value: reduce [ prek/br_ugov prek/serija ] ]
    tabla/ablak:  ablak_b
    tabla_view
  ]
  tv
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
      at (as-pair (meretb + 20) (merety - 20)) slider (as-pair (meretx - meretb - 40) 20) with [ user-data: ablak_c ]
      [ scroller_pozicio face ]
    ] 0x0
    ablak_b/user-data: 0x0
    ablak_c/user-data: 0x0
    main_window/user-data: ablak_c ; gorgo bekapcsolasa
    show ablak_a
    ;
    ablak_brajz
    ablak_cezar
  ]
  
  set 'evid_pur: func [ /local sz1 sz0 row1 blk0 ]
  [
    sif_nar: none
    pl_dok:  none 
    dat0: sistime/isodate
    god0: baze_sif
    god_list: copy/deep baze_izbor
    faza_blk: make block! [ [ "" "P" "Z" ] [ "Otvoren" "P-Poslat" "Z-Zatvoren" ] ]
    ;
    ablak_arajz
    if not empty? nyomelem
    [
      tabla/akcio (reduce [ nyomelem/1 nyomelem/2 ])
      clear nyomelem
    ]
  ]
]

append cleanup
[
  evid_pur_def_01
  evid_pur
  prek
  pur_adatok_olvas
  pur_podaci
]