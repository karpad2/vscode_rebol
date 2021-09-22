;
; Materijal-Nov
;
; hasznal: program_mode
;

materijal_nov_01: context
[
  klas_oblast: none ; oblast: Mat
  
  klas_tar:    none ; teljes klasifikacios tar
  klas_mbr:    none ; kezdo rekord sorszama
  filter_rek:  none ; filterezes a rekordnal
  filter_tab:  none ; filterezes a tablazatnal
  filter_blk:  none ; aktualis filterezes az elso ketto valamelyikebol
  formak_blk:  none ; aktualis formak taroloja
  valtoz_blk:  none ; valtozok blokkja
  tname:       none ; tablanev
  g_baze_sif:  none ; bazis - mezok nevei
  ; belso definiciok:
  mater_sif:   copy ""
  novi_sif:    copy ""
  jed_mere:    copy "" ; a sifra masolaskor hasznalom
  jm_lista:    none
  akt_fld:     none ; aktualis mezo a toltesre
  rek:         none ; rekord materijala
  digits:   charset "0123456789"  
  ablak_p:     none ; seged-ablak
  ablak_q:     none ; seged-ablak
  keres_rek:   none ; rekord a keresesre
  bris_list:   none ; leszalado lista a bris toltesere
  tarif_tar:   none ; tarifa szamok tarja
  plan_naziv:  copy ""
  izv_mode: make object!
  [
    izb1: false
  ]
  g_bris_list: 
  [
    [ "A+B" "B" "I" "" ]
    [ "A+B - aktivni i predv.bris (sa stanjem) "  "B - predv.bris (bez stanja)" "I - neaktivni" "" ]
  ]
  ; 
  
  osvezi_nazmat_druga_tabela: func [ /local fp1 ]
  [
    fp1: flash "Obrada podataka..."
    ;-- sastavnica
    mysql_cmd
    [
      "UPDATE materijal AS t1 "
      "LEFT JOIN sastavnica AS t2 ON t2.niza_sif=CONCAT('M-',t1.sif_mat) "
      "SET t2.naziv=t1.naz_mat,t2.opis=t1.name "
      "WHERE t1.bris='A' AND (LENGTH(t1.naz_mat)>80 OR LENGTH(t1.name)>80) AND t2.niza_sif IS NOT NULL"
    ]
    ;-- sastav_mat
    mysql_cmd
    [
      "UPDATE materijal AS t1 "
      "LEFT JOIN sastav_mat AS t2 ON t2.sif_mat0=t1.sif_mat "
      "SET t2.naziv=t1.naz_mat,t2.name=t1.name "
      "WHERE t1.bris='A' AND (LENGTH(t1.naz_mat)>80 OR LENGTH(t1.name)>80) AND t2.sif_mat IS NOT NULL"
    ]
    ;-- sasmod_mat
    mysql_cmd
    [
      "UPDATE materijal AS t1 "
      "LEFT JOIN sasmod_mat AS t2 ON t2.sif_mat0=t1.sif_mat "
      "SET t2.naziv=t1.naz_mat,t2.name=t1.name "
      "WHERE t1.bris='A' AND (LENGTH(t1.naz_mat)>80 OR LENGTH(t1.name)>80) AND t2.sif_mat IS NOT NULL"
    ]
    ;-- msort_materijal
    mysql_cmd
    [
      "UPDATE materijal AS t1 "
      "LEFT JOIN msort_materijal AS t2 ON t2.sif_robe=t1.sif_mat "
      "SET t2.naz_robe=t1.naz_mat "
      "WHERE t1.bris='A' AND (LENGTH(t1.naz_mat)>80 OR LENGTH(t1.name)>80) AND t2.sif_robe IS NOT NULL"
    ]
    ;-- nabavka
    mysql_cmd
    [
      "UPDATE materijal AS t1 "
      "LEFT JOIN nabavka AS t2 ON t2.sif_robe=CONCAT('M-',t1.sif_mat) "
      "SET t2.naz_robe=t1.naz_mat "
      "WHERE t1.bris='A' AND (LENGTH(t1.naz_mat)>80 OR LENGTH(t1.name)>80) AND t2.sif_robe IS NOT NULL"
    ]
    ;-- prom_potnab
    mysql_cmd
    [
      "UPDATE materijal AS t1 "
      "LEFT JOIN prom_potnab AS t2 ON t2.sif_robe=CONCAT('M-',t1.sif_mat) "
      "SET t2.naziv=t1.naz_mat "
      "WHERE t1.bris='A' AND (LENGTH(t1.naz_mat)>80 OR LENGTH(t1.name)>80) AND t2.sif_robe IS NOT NULL"
    ]
    ;-- rezervacija
    mysql_cmd
    [
      "UPDATE materijal AS t1 "
      "LEFT JOIN rezervacija AS t2 ON t2.sif_mat0=t1.sif_mat "
      "SET t2.naziv=t1.naz_mat,t2.opis=t1.name "
      "WHERE t1.bris='A' AND (LENGTH(t1.naz_mat)>80 OR LENGTH(t1.name)>80) AND t2.sif_mat IS NOT NULL"
    ]
    unview/only fp1
  ]
  
  osvez_sir_grupa: func [ /local mat_tar row0 row1 sz0 fp1 ]
  [
    blk0: make block! []  ;-- lista azokrol a sifrakrol, amelyekre nincs talalat a sirovinak kozott
    sz0:  copy ""
    fp1: flash "Obrada podataka"
    ;
    mysql_cmd
    [
      "SELECT COUNT(*) FROM sirovina AS t1 WHERE t1.grupa='plast2' "
    ]
    if not empty? row1: first db
    [
      if (int_value row1/1/1) = 0
      [
        alert "Fali grupa: plast2 i sifra: ostali u sirovine!"
      ]
    ]
    ;
    dok_path: request-file/title/filter/keep/only "Izbor documenta" "Izbor" "*.csv" dir_eximp
    if not file? dok_path [ exit ]
    tar0: read/lines dok_path
    ;
    foreach row0 tar0
    [
      blk0: parse/all row0 "|"
      mysql_cmd
      [
        "UPDATE materijal AS t1 "
        "SET t1.grupa_mat='" blk0/2 "',t1.podgr_mat='" blk0/3 "',t1.sir_mat='" blk0/4 "' "
        "WHERE t1.sif_mat='" blk0/1 "'"
      ]
    ]
    ;
    mysql_cmd
    [
      "SELECT t1.sif_mat,t1.naz_mat,t1.grupa_mat,t1.podgr_mat,t1.sir_mat,t1.bris "
      "FROM materijal AS t1 "
      "WHERE t1.grupa_mat='plast2'"
    ]
    mat_tar: get_rekordset copy db
    ;
    foreach row1 mat_tar
    [
      ;-- javitom a sastavnicaban a podgrupat
      sz0: rejoin [ "M-" row1/sif_mat ]
      mysql_cmd
      [
        "UPDATE sastavnica AS t1 "
        "SET t1.podgr='" row1/podgr_mat "' "
        "WHERE t1.niza_sif='" sz0 "' "
      ]
    ]
    unview/only fp1
  ]
  
  zadnje_cene_sql_select: func [ sifra[string!] magacin[integer!] tip[integer!] /local god god1 god2 dat0 ]
  [
    god:  now/year
    god1: now/year - 1
    god2: now/year - 2
    dat0: to-iso-date ((to-date sistime/isodate) - 730)
    ;
    if tip = 1
    [
      mysql_cmd
      [
        "SELECT t1.sif_robe,t1.naz_robe,t1.datum_dok,t1.ulaz,t1.sif_mag, "
        "IF(t1.jed_cena = 0,(SELECT t4.jed_cena FROM " firma/baze "_" god ".prijem AS t4 "
                            "WHERE t4.dok_prijema=t1.br_dok AND t4.dok_mbr=t1.mbr LIMIT 1),t1.jed_cena) AS jed_cena,"
        "(SELECT (SELECT t6.sif_part FROM " firma/baze "_" god ".dok_prij AS t6 WHERE t6.br_dok=t5.br_dok) "
         "FROM " firma/baze "_" god ".prijem AS t5 WHERE t5.dok_prijema=t1.br_dok AND t5.dok_mbr=t1.mbr LIMIT 1) AS sif_part "
        "FROM " firma/baze "_" god ".msort_materijal AS t1 "
        "WHERE t1.sif_robe=" apjel sifra apjel " AND t1.ulaz>0 AND t1.sif_mag=" magacin " AND t1.vrsta_pr=10 AND t1.datum_dok>='" dat0 "' HAVING jed_cena>0 "
        "UNION "
        "SELECT t1.sif_robe,t1.naz_robe,t1.datum_dok,t1.ulaz,t1.sif_mag, "
        "IF(t1.jed_cena = 0,(SELECT t4.jed_cena FROM " firma/baze "_" god1 ".prijem AS t4 "
                            "WHERE t4.dok_prijema=t1.br_dok AND t4.dok_mbr=t1.mbr LIMIT 1),t1.jed_cena) AS jed_cena,"
        "(SELECT (SELECT t6.sif_part FROM " firma/baze "_" god1 ".dok_prij AS t6 WHERE t6.br_dok=t5.br_dok) "
         "FROM " firma/baze "_" god1 ".prijem AS t5 WHERE t5.dok_prijema=t1.br_dok AND t5.dok_mbr=t1.mbr LIMIT 1) AS sif_part "
        "FROM " firma/baze "_" god1 ".msort_materijal AS t1 "
        "WHERE t1.sif_robe= " apjel sifra apjel " AND t1.ulaz>0 AND t1.sif_mag=" magacin " AND t1.vrsta_pr=10 AND t1.datum_dok>='" dat0 "' HAVING jed_cena>0 "
        "UNION "
        "SELECT t1.sif_robe,t1.naz_robe,t1.datum_dok,t1.ulaz,t1.sif_mag, "
        "IF(t1.jed_cena = 0,(SELECT t4.jed_cena FROM " firma/baze "_" god2 ".prijem AS t4 "
                            "WHERE t4.dok_prijema=t1.br_dok AND t4.dok_mbr=t1.mbr LIMIT 1),t1.jed_cena) AS jed_cena,"
        "(SELECT (SELECT t6.sif_part FROM " firma/baze "_" god2 ".dok_prij AS t6 WHERE t6.br_dok=t5.br_dok) "
         "FROM " firma/baze "_" god2 ".prijem AS t5 WHERE t5.dok_prijema=t1.br_dok AND t5.dok_mbr=t1.mbr LIMIT 1) AS sif_part "
        "FROM " firma/baze "_" god2 ".msort_materijal AS t1 "
        "WHERE t1.sif_robe= " apjel sifra apjel " AND t1.ulaz>0 AND t1.sif_mag=" magacin " AND t1.vrsta_pr=10 AND t1.datum_dok>='" dat0 "' HAVING jed_cena>0 "
        "ORDER BY datum_dok DESC LIMIT 5 "
      ]
    ]
    if tip = 2 
    [
      mysql_cmd
      [
        "SELECT t1.sif_robe,t1.naz_robe,t1.datum_dok,t1.ulaz,"
        "IF(t1.jed_cena = 0,(SELECT t4.jed_cena FROM " firma/baze "_" god ".prijem AS t4 "
                            "WHERE t4.dok_prijema=t1.br_dok AND t4.dok_mbr=t1.mbr LIMIT 1),t1.jed_cena) AS jed_cena "
        "FROM " firma/baze "_" god ".msort_materijal AS t1 "
        "WHERE t1.sif_robe=" apjel sifra apjel " AND t1.ulaz>0 AND t1.sif_mag=" magacin " AND t1.vrsta_pr=10 AND t1.datum_dok>='" dat0 "' HAVING jed_cena>0 "
        "UNION "
        "SELECT t1.sif_robe,t1.naz_robe,t1.datum_dok,t1.ulaz,"
        "IF(t1.jed_cena = 0,(SELECT t4.jed_cena FROM " firma/baze "_" god1 ".prijem AS t4 "
                            "WHERE t4.dok_prijema=t1.br_dok AND t4.dok_mbr=t1.mbr LIMIT 1),t1.jed_cena) AS jed_cena "
        "FROM " firma/baze "_" god1 ".msort_materijal AS t1 "
        "WHERE t1.sif_robe= " apjel sifra apjel " AND t1.ulaz>0 AND t1.sif_mag=" magacin " AND t1.vrsta_pr=10 AND t1.datum_dok>='" dat0 "' HAVING jed_cena>0 "
        "UNION "
        "SELECT t1.sif_robe,t1.naz_robe,t1.datum_dok,t1.ulaz,"
        "IF(t1.jed_cena = 0,(SELECT t4.jed_cena FROM " firma/baze "_" god2 ".prijem AS t4 "
                            "WHERE t4.dok_prijema=t1.br_dok AND t4.dok_mbr=t1.mbr LIMIT 1),t1.jed_cena) AS jed_cena "
        "FROM " firma/baze "_" god2 ".msort_materijal AS t1 "
        "WHERE t1.sif_robe= " apjel sifra apjel " AND t1.ulaz>0 AND t1.sif_mag=" magacin " AND t1.vrsta_pr=10 AND t1.datum_dok>='" dat0 "' HAVING jed_cena>0 "
        "ORDER BY datum_dok DESC LIMIT 5 "
      ]
    ]
    tar1: get_rekordset copy db
    return tar1
  ]
  
  zadnje_cene_prosek: func [ sif_mat[string!] magacin[integer!] tip[integer!] mode[string!] /local tar1 tar2 row0 row2 row3 ar_osszeg ar_osszeg_eu 
                             uj_arak uj_arak_eu uj_atl uj_atl_eu atlag_ar atlag_ar_eu kurs ]
  [
    ar_osszeg:    0
    ar_osszeg_eu: 0
    atlag_ar:     0
    atlag_ar_eu:  0
    uj_atl:       0
    uj_atl_eu:    0
    pros_cene: make block! [ 0 0 "Ne" ]
    tar1: zadnje_cene_sql_select sif_mat magacin tip
    if empty? tar1 [ return pros_cene ] 
    ;legujabb_ar: (to-decimal tar1/1/jed_cena) 
    tar2: make block! []
    foreach row0 tar1
    [
      if (now - (to-date row0/datum_dok)) < 180
      [
        append tar2 row0
      ]
    ]
    
    append tar1 tar2

    drb_szam: length? tar1
    
    foreach row2 tar1
    [
      ar_osszeg: ar_osszeg + (to-decimal row2/jed_cena)
      kurs: kurs_valute_nadan "EUR" row2/datum_dok
      ar_osszeg_eu: ar_osszeg_eu + ((to-decimal row2/jed_cena) / (to-decimal kurs))
    ]
    atlag_ar: ar_osszeg / drb_szam
    atlag_ar_eu: ar_osszeg_eu / drb_szam
     
    either not empty? tar2
    [
      foreach row3 tar2
      [
        uj_atl: uj_atl + (to-decimal row3/jed_cena)
        kurs: kurs_valute_nadan "EUR" row3/datum_dok
        uj_atl_eu: uj_atl_eu + ((to-decimal row3/jed_cena) / (to-decimal kurs))
      ]
      uj_arak: uj_atl / (length? tar2)
      uj_arak_eu: uj_atl_eu / (length? tar2)
    ]
    [
      uj_arak: (to-decimal tar1/1/jed_cena)
      kurs: kurs_valute_nadan "EUR" tar1/1/datum_dok
      uj_arak_eu: (to-decimal tar1/1/jed_cena) / (to-decimal kurs)
    ]
    either (atlag_ar > (uj_arak + (uj_arak * 0.2))) or (atlag_ar < (uj_arak - (uj_arak * 0.2)))
    [
      either mode = "test"
      [
        pros_cene/1: 0
      ]
      [
        pros_cene/1: (dec_form uj_arak 0 4)
      ]
      pros_cene/3: "Da" ; van 20% +- különbség
    ]
    [
      pros_cene/1: (dec_form atlag_ar 0 4)
    ]
    either (atlag_ar_eu > (uj_arak_eu + (uj_arak_eu * 0.2))) or (atlag_ar_eu < (uj_arak_eu - (uj_arak_eu * 0.2)))
    [
      either mode = "test"
      [
        pros_cene/2: 0
      ]
      [
        pros_cene/2: (dec_form uj_arak_eu 0 4)
      ]
    ]
    [
      pros_cene/2: (dec_form atlag_ar_eu 0 4)
    ]
    return pros_cene
  ]
  
  zadnje_cene_mat_ablak: func [ arak[block!] /local arak_str row1 rek0 p1 pod_ncena_blk prosek_cena_blk prosek_cena_blk_a kurs eur_cena ]
  [
    pod_ncena_blk: parse/all rek/pod_ncena "^/"
    prosek_cena_blk: parse/all pod_ncena_blk/1 "|"
    prosek_cena_blk_a: parse/all pod_ncena_blk/2 "|"
    tabla_blk: compose
    [
      size 1095x495
      backtile polished color_g4
      style tlab text with [ edge: [color: coal size: 1x1 ] font: [ space: 0x-2 ] para: [ origin: 1x2 ] ]
      across
      at 9x9 button close_btn_img 26x26 keycode escape
      [
        hide-popup
      ]
      at  40x9 text "<ESC>" 70x24
      at 110x9 text right "Kalk.nab.cena A:"        120x24
      at 230x9 info right (prosek_cena_blk_a/2)       110x24 0.128.255
      at 340x9 text left "Din" 50x24
      at 375x9 info right (prosek_cena_blk_a/3)       110x24 0.128.255
      at 485x9 text left "EUR" 50x24
      at 540x9 text (rejoin [ "(" prosek_cena_blk_a/4 ", " prosek_cena_blk_a/5 ")" ]) 200x24
      
      at 110x39  text right "Kalk.nab.cena B:"        120x24
      at 230x39  info right (prosek_cena_blk/2)         110x24 181.230.29
      at 340x39 text left "Din" 50x24
      at 375x39  info right (prosek_cena_blk/3)       110x24 181.230.29
      at 485x39 text left "EUR" 50x24
      at 540x39 text (rejoin [ "(" prosek_cena_blk/4 ", " prosek_cena_blk/5 ")" ]) 200x24
      ;
      at 40x81  tlab center middle "Mag."            100x35 color_h1 color_h2
      at 139x81 tlab center middle "Datum"           100x35 color_h1 color_h2
      at 238x81 tlab center middle "Jed. cena"       100x35 color_h1 color_h2
      at 337x81 tlab center middle "Jed. cena EUR"   100x35 color_h1 color_h2
      at 436x81 tlab center middle "Ulaz"            100x35 color_h1 color_h2
      at 535x81 tlab center middle "Sif. Partnera"   100x35 color_h1 color_h2
      at 634x81 tlab center middle "Naziv Partnera"  420x35 color_h1 color_h2
    ]
    p1: 40x115
    foreach row1 arak
    [
      mysql_cmd
      [
        "SELECT t1.name1 FROM partner AS t1 WHERE t1.sifra='" row1/sif_part "' "
      ]
      rek0: get_rekord first db
      if (to-integer row1/sif_mag) = 1
      [
        append tabla_blk compose
        [
          at (p1) tlab center middle (row1/sif_mag) 100x35 00.00.00 181.230.29
        ]
      ]
      if (to-integer row1/sif_mag) = 16
      [
        append tabla_blk compose
        [
          at (p1) tlab center middle (row1/sif_mag) 100x35 00.00.00 0.128.255
        ]
      ]
      kurs: kurs_valute_nadan "EUR" row1/datum_dok
      eur_cena: (to-decimal row1/jed_cena) / kurs 
      append tabla_blk compose
      [ 
        at (p1 + 99x0)  tlab center middle (row1/datum_dok) 100x35 color_t1 color_t2
        at (p1 + 198x0) tlab right  middle (dec_form row1/jed_cena 0 4) 100x35 color_t1 color_t2
        at (p1 + 297x0) tlab right  middle (dec_form eur_cena 0 4)      100x35 color_t1 color_t2
        at (p1 + 396x0) tlab right  middle (dec_form row1/ulaz 0 4)     100x35 color_t1 color_t2
        at (p1 + 495x0) tlab center middle (row1/sif_part)  100x35 color_t1 color_t2
        at (p1 + 594x0) tlab left   middle (rek0/name1)     420x35 color_t1 color_t2
      ]
      p1: p1 + 0x34
    ]
    ablak_p: layout/offset tabla_blk 0x0
    ;
    inform/title ablak_p "Zadnje cene materijala"
  ]
  
  ablak_tarif_car: func [ /local psz pmag rek0 blk0 blk1 poz0 row0 sz0 sz1 ]
  [
    psz: 620 pmag: 300
    ;
    tarif_tar: make block! []
    blk0:      make block! []
    blk1:      make block! [[][]]
    sz0:       copy ""
    sz1:       copy ""
    ;
    mysql_cmd
    [
    	"SELECT t1.tarif_car,t1.oblik_car FROM materijal AS t1 "
			"WHERE t1.sif_mat='" rek/sif_mat "'"
  	]
    rek0: get_rekord first db
    blk0: parse/all rek0/oblik_car "|"
    poz0: head blk0
    while [ not tail? poz0 ]
  	[
  		blk1: parse/all poz0/1 ";"
  		append tarif_tar make object!
  		[
  			tarif: copy blk1/1
  			opis:  copy blk1/2
  		]
  		poz0: next poz0
  	]
  	if (rek0/tarif_car <> "") and (rek0/oblik_car = "")
  	[
  		append tarif_tar make object!
  		[
  			tarif: copy rek0/tarif_car
  			opis:  copy ""
  		]
  	]
    ;
    tabla_blk: compose
    [
      size (as-pair psz pmag)
      backtile polished color_g2
      across
      at 9x9 button close_btn_img 26x26 with [ keycode: #"^(1b)" ]
      [
        hide-popup
      ]
    ]
    if block? find kor_specijal "tarcarmat"
    [
	    append tabla_blk compose
	    [
	      at 70x9 button ~"Modif" 95x24 color_m2 
	      [
	      	sz0: copy ""
	      	sz1: copy ""
	      	foreach row0 tarif_tar
	      	[
	      		if (row0/tarif = "") and (row0/opis  <> "") [ alert "Upisati tarifa carine!"      exit ]
	      		if (row0/opis  = "") and (row0/tarif <> "") [ alert "Upisati opis tarifa carine!" exit ]
	      		if (row0/tarif <> "") and (row0/opis  <> "")
	      		[
	      			if sz1 =  "" [ sz1: copy row0/tarif ]
		      		if sz0 <> "" [ append sz0 "|" ]
		      		append sz0 (rejoin [ row0/tarif ";" row0/opis ])
	      		]
	      	]
	      	mysql_cmd
			    [
			    	"UPDATE materijal AS t1 "
			    	"SET t1.tarif_car='" sz1 "',t1.oblik_car='" sz0 "' "
						"WHERE t1.sif_mat='" rek/sif_mat "'"
			  	]
			  	rek/oblik_car: copy sz0
			  	mod_mater
	      	hide-popup
	      ]
	    ]
    ]
    append tabla_blk compose
    [
    	at 0x40 ablak_q: box color_g4 (as-pair psz (pmag - 40)) with [ edge: [color: coal size: 1x1 ] ]
    ]
    ;
    ablak_p: layout/offset tabla_blk 0x0
    tarif_car_olvas
    inform/title ablak_p "Tarifa carine"
  ]
  
  tarif_car_olvas: func [ /local p1 psz pmag i1 n1 ]
  [
    psz: 620 pmag: 300
    blk0: make block! []
    ;
    p1: 40x10
    tabla_blk: compose
    [
      size ablak_q/size
      style t_nev text right 90x24
      style t_val info 100x24 color_g4
      style tlab text with [ edge: [color: coal size: 1x1 ]]
      backdrop color_g4
      across space -1x8
      ;
    ]
    append tabla_blk compose
    [
      at (p1)
      tlab center "Rbr"            41x28 color_h1 color_h2
      tlab center "Tarifa carine" 121x28 color_h1 color_h2
      tlab center "Opis"          391x28 color_h1 color_h2
    ]
    ;
    i1: 0
    n1: 0
    ;
		foreach row0 tarif_tar
		[	
			i1: i1 + 1
			p1: p1 + 0x27
			either block? find kor_specijal "tarcarmat"
    	[
	  		append tabla_blk compose
	      [
	        at (p1)
	        tlab  center (to-string i1) 41x28 color_t1 color_b2
	        field left   (row0/tarif)  121x28
	        field left   (row0/opis)   391x28
	      ]
      ]
      [
	  		append tabla_blk compose
	      [
	        at (p1)
	        tlab center (to-string i1)  41x28 color_t1 color_b2
	        tlab center (row0/tarif)   121x28 color_t1 color_t2
	        tlab left   (row0/opis)    391x28 color_t1 color_t2
	      ]
      ]
		]
		if block? find kor_specijal "tarcarmat"
		[
			p1: p1 + 0x27
			append tabla_blk compose
	    [
	    	at (p1)
				tlab center "Ins"  41x28 color_t1 color_b2
				[
					append tarif_tar make object!
					[
						tarif: copy ""
						opis:  copy ""
					]
					tarif_car_olvas
				]
			]
		]
    ablak_q/pane: layout/offset tabla_blk 0x0
    show ablak_q
  ]
  
  ablak_izvestaji: func [ /local mat_oour bmode ]
  [
    mat_oour: copy "" 
    tabla_blk: compose/only
    [
      size ablak_c/size
      backdrop color_g4
      across
      at 9x9 button close_btn_img 26x26
      [
        ablak_cezar
      ]
      at 40x10 text as-is rejoin
      [
        "Izveštaji prikazuju izabrane materijele iz leve tabele!^/"
        "Redosled podataka odgovara redosledu u tabeli.^/"
        "(Po potrebi resetovati filter materijala)^/"
      ] 400x60
      at 10x90 text "Stanje do datuma:" 140x24
      at 150x90 w_date pwork/datum2 120x24
      at 310x90 text "Samo sa stanjem:" 140x24
      at 450x90 check (izv_mode/izb1) 24x24
      [
        izv_mode/izb1: face/data
      ]
      at 10x140 text "Pregled osnovnih podataka materijala:" 300x24
      at 310x140 button "Pregled podataka" 180x24 color_l2
      [
        ;alert "U fazi izrade" exit
        tabla_view
        stamp_mater_pdf_nov izv_mode
      ]
      ; inkabb ne maradjon benne, mert szinte hasznalhatalan a sok adat miatt
      ;at 10x190 text "Pregled nabavnih podataka materijala:" 300x24
      ;at 310x190 button "Nabavni podaci" 180x24 color_l2
      ;[
      ;  tabla_view
      ;  nabav_podaci_mat_pdf izv_mode
      ;]
      at 10x190 text "Stanje sortirano po grupi,podgr. i obliku:" 300x24
      at 310x190 button "Stanje magac.po grup." 180x24 color_l2
      [
        tabla_view
        stamp_matobl_pdf_nov izv_mode
      ]
      at 10x240 text "Stanje materijala na dan:" 300x24
      at 310x240 button "Stanje materijala" 180x24 color_l2
      [
        tabla_view
        stamp_matkat_pdf_nov izv_mode
      ]
      at 10x290 text "Analiza promene cene materijala:" 300x24
      at 310x290 button "Analiza promene cene" 180x24 color_l2
      [
        stamp_analiza_prom_cene_mat2 
      ]
      at  10x340 text   "Izveštaj kalkulisane nabavne cene:" 300x24
      at 255x341 drop-down 50x24 data g_orgjed/1 
      with [ rows: 4 text: ( blk_find g_orgjed/1 g_orgjed/2 tabla/param/1) ]
      [ 
        if string? face/data
        [
          mat_oour: face/data
        ]
      ]
      at 310x340 button "Izveštaj kalk.nab.cene" 180x24 color_l2
      [
        if mat_oour = "" [ alert "Izaberite lokaciju!" exit ]
        kalk_nabavne_cene_xml mat_oour
      ]
      at  10x390 text " Osveži kalk.nab.cene:" 250x24
      at 310x390 button "Obrada" 100x24 color_m2
      [
        fp1:  flash "Uèitavanje podataka..."
        mysql_cmd
        [
          "SELECT DISTINCT t1.sif_mat FROM materijal AS t1 WHERE (t1.tip='R' OR t1.tip='T') "
        ]
        tar0: get_rekordset copy db
        ;
        sorok: 0
        either firma/baze = "test" [ bmode: "test" ][ bmode: "normal" ]
        foreach row0 tar0
        [
          atlag_ar_1: make block! []
          atlag_ar_1: zadnje_cene_prosek row0/sif_mat 1 2 bmode
          atlag_str_1: rejoin [ "1|" atlag_ar_1/1 "|" atlag_ar_1/2 "|" ( to-iso-date now ) "|" korisnik "|" atlag_ar_1/3 ]
          ;**********************************************************************************************
          atlag_ar_16: make block! []
          atlag_ar_16: zadnje_cene_prosek row0/sif_mat 16 2 bmode
          atlag_str_16: rejoin [ "16|" atlag_ar_16/1 "|" atlag_ar_16/2 "|" ( to-iso-date now ) "|" korisnik "|" atlag_ar_16/3 ]
          ;
          kalk_pod: rejoin [ atlag_str_1 "^/" atlag_str_16 ]
          ;
          mysql_cmd
          [
            "UPDATE materijal SET pod_ncena='" kalk_pod "' WHERE sif_mat='" row0/sif_mat "' " 
          ]
          if retval
          [
            sorok: sorok + 1
          ]
        ]
        ;
        unview/only fp1
      ]
    ]
    if firma_rek/partner_baze_pod <> ""
    [
      append tabla_blk compose
      [
        at  10X440 text "Transfer podataka materijala:" 300x24
        at 310x440 button "Transfer pod.mater." 180x24 color_m2
        [
          logfh: make string! 1000
          transfer_podataka_partnera "M" ""
        ]
      ]
    ]
    if find [ "prg" "adm" ] dozvola
    [
      append tabla_blk compose
      [
        at  10x470 text "Osveži naziv materijala druga tabela:" 300x24
        at 310x470 button "Osveži naziv.mat." 180x24 color_m2
        [
          osvezi_nazmat_druga_tabela
        ]
        at 310x520 button "Sir. frissit" 140x24 color_m2
        [
          sirovina_frissit
        ]
        at 310x550 button "Naziv generator" 140x24 color_m2
        [
          automata_naziv_osszerako
        ]
        at 310x580 button "Osvež.car.tarif" 140x24 color_m2
        [
          if korisnik <> "csa" [ alert "Nije dozvoljeno!" exit ]
          osvez_car_tarif
        ]
        at 310x610 button "Osvež.name mat." 140x24 color_m2
        [
          if korisnik <> "csa" [ alert "Nije dozvoljeno!" exit ]
          osvez_name_mat
        ]
        at 310x640 button "Osvež.plast2" 140x24 color_m2
        [
          if korisnik <> "csa" [ alert "Nije dozvoljeno!" exit ]
          osvez_sir_grupa
        ]
      ]
    ]
    ablak_c/pane: layout/offset tabla_blk ablak_c/user-data
    ablak_q/user-data: 0x0
    parameter_filter
    show ablak_c
  ]
  ;
  ablak_cezar: func [ ]
  [
    ;filter_tab alapjan dolgozik
    filter_blk: filter_tab
    mater_sif: copy ""
    tabla_blk: compose
    [
      size ablak_c/size
      backdrop color_g4
      across
      at 10x5 btn "Sz:M" 40x24 color_l2
      [
        set_window_size 'm
        ablak_arajz
        mat_tablazat
        ablak_cezar
      ]
      at 60x5 h3 "Izbor materijala:" 150x24 [ ]
    ]
    ;
    append tabla_blk compose/only
    [
      at 210x5 text right "Izbor:" 50x24
      at 260x5 drop-down 320x24 data g_bris_list/2
      with [ rows: 4 text: blk_find g_bris_list/1 g_bris_list/2 keres_rek/bris ]
      [
        if string? face/data
        [
          keres_rek/bris: blk_find g_bris_list/2 g_bris_list/1 face/text
          mat_tablazat
        ]
      ]
      at   0x35 text  right "Deo Naziva:" 120x24 
      at 120x35 field keres_rek/nazdeo    460x24
      [
        mat_tablazat
      ]
      at   0x60 text  right "Standard:"  120x24 
      at 120x60 field keres_rek/standard 460x24
      [
        mat_tablazat
      ]
      at   0x85 text  right "Katal. broj:" 120x24
      at 120x85 field keres_rek/katbr1     460x24
      [
        mat_tablazat
      ]
      at   0x110 text  right "Proizvoðaè:" 120x24
      at 120x110 field keres_rek/proizv    460x24
      [
        mat_tablazat
      ]
      ; 
      at 0x140 ablak_q: box color_g4 (ablak_c/size - 0x140) with [  edge: [color: coal size: 1x1 ] ]
    ]
    ablak_c/pane: layout/offset tabla_blk ablak_c/user-data
    ablak_q/user-data: 0x0
    parameter_filter
    show ablak_c
  ]
  ;
  brisanje_materijala: func [ mrek[object!] ]
  [
    if mrek/promet > 0  [ return "Postoji promet Materijala!" ]
    if mrek/sastav > 0  [ return "Materijal se koristi u sastavnici!" ]
    if mrek/techno > 0  [ return "Materijal ima tehnologiju izrade!" ]
    if mrek/rezerv > 0  [ return "Materijal se koristi kod rezervacije naloga!" ]
    if mrek/nabavka > 0 [ return "Materijal se koristi u nabavci!" ]
    ;
    mysql_cmd [ "DELETE FROM lok_stavke WHERE sif_stavke='M-" mater_sif "' " ]
    if not mysts [ return false ]
    ;
    mysql_cmd [ "DELETE FROM nablist WHERE sif_robe='M-" mater_sif "' " ]
    if not mysts [ return false ]
    ;
    mysql_cmd [ "DELETE FROM lok_mater WHERE sif_robe='" mater_sif "' " ]
    if not mysts [ return false ]
    mysql_cmd [ "DELETE FROM materijal WHERE sif_mat='" mater_sif "' LIMIT 1" ]
    if not mysts [ return false ]
    del_hist_add "materijal" mater_sif
    return true
  ]

  zamena_materijala: func [ mrek[object!] /local mat0 ]
  [
    if mrek/promet > 0  [ return "Postoji promet Materijala!" ]
    if mrek/sifra = "" [ return "Zadati šifru zamene!" ]
    mysql_cmd [ "SELECT * FROM materijal WHERE sif_mat='" mrek/sifra "' " ]
    mat0: get_rekord first db
    if not retval [ return "Šifra zamene nije u bazi!" ]
    if rek/tip <> mat0/tip [ return rejoin [ "Tip zamene nije '" rek/tip "' !"] ]
    if rek/oblik <> mat0/oblik [ return rejoin [ "Oblik zamene nije '" rek/oblik "' !"] ]
    if rek/jed_mere <> mat0/jed_mere [ return rejoin [ "Jed.mera zamene nije '" rek/jed_mere "' !"] ]
    if mater_sif = mat0/sif_mat [ return "Šifra zamene mora biti razlièita!" ]
    mysql_cmd
    [
      "UPDATE sastavnica SET niza_sif='M-" mat0/sif_mat "',tip='" mat0/tip "',naziv='" mat0/naz_mat "',"
      "jed_mere='" mat0/jed_mere "',oblik='" mat0/oblik "',podgr='" mat0/podgr_mat "' "
      "WHERE niza_sif='M-" mater_sif "' "
    ]
    if not mysts [ return false ]
    mysql_cmd
    [
      "UPDATE rezervacija SET sif_mat='M-" mat0/sif_mat "',sif_mat0='" mat0/sif_mat "',naziv='" mat0/naz_mat "',"
      "jed_mere='" mat0/jed_mere "',oblik='" mat0/oblik "',podgr='" mat0/podgr_mat "' "
      "WHERE sif_mat='M-" mater_sif "' "
    ]
    if not mysts [ return false ]
    mysql_cmd
    [
      "UPDATE tehnolog SET sifra='M-" mat0/sif_mat "' WHERE sifra='M-" mater_sif "' "
    ]
    if not mysts [ return false ]
    mysql_cmd
    [
      "UPDATE tehn_oper SET sifra='M-" mat0/sif_mat "' WHERE sifra='M-" mater_sif "' "
    ]
    if not mysts [ return false ]
    mysql_cmd
    [
      "UPDATE tehn_roba SET sifra='M-" mat0/sif_mat "' WHERE sifra='M-" mater_sif "' "
    ]
    if not mysts [ return false ]
    mysql_cmd
    [
      "UPDATE tehn_roba SET sif_robe='M-" mat0/sif_mat "',opis='" mat0/naz_mat "' "
      "WHERE sif_robe='M-" mater_sif "' "
    ]
    if not mysts [ return false ]
    mysql_cmd
    [
      "UPDATE nabavka SET sif_robe='M-" mat0/sif_mat "',naz_robe='" mat0/naz_mat "' "
      "WHERE sif_robe='M-" mater_sif "' "
    ]
    if not mysts [ return false ]
    mysql_cmd
    [
      "UPDATE prom_potnab SET sif_robe='M-" mat0/sif_mat "',naziv='" mat0/naz_mat "' "
      "WHERE sif_robe='M-" mater_sif "' "
    ]
    if not mysts [ return false ]
    ;
    mysql_cmd [ "DELETE FROM lok_stavke WHERE sif_stavke='M-" mater_sif "' " ]
    if not mysts [ return false ]
    mysql_cmd [ "DELETE FROM lok_mater WHERE sif_robe='" mater_sif "' " ]
    if not mysts [ return false ]
    mysql_cmd [ "DELETE FROM materijal WHERE sif_mat='" mater_sif "' LIMIT 1" ]
    if not mysts [ return false ]
    del_hist_add "materijal" mater_sif
    return true
  ]
  
  kontrola_brisanje_materijala: func [ /local mrek psz c1 c2 pmag hb ]
  [
    mrek: make object! compose
    [
      sifra:  (copy "")
      naziv:  (copy "")
      promet:  0
      sastav:  0
      techno:  0
      rezerv:  0
      nabavka: 0
    ]
    mysql_cmd [ "SELECT count(*) FROM msort_materijal WHERE sif_robe ='" mater_sif "' " ]
    mrek/promet: to-integer first first first db
    mysql_cmd [ "SELECT count(*) FROM prijem WHERE sif_robe ='" mater_sif "' " ]
    mrek/promet: mrek/promet + (to-integer first first first db)
    mysql_cmd [ "SELECT count(*) FROM sastavnica WHERE niza_sif='M-" mater_sif "' " ]
    mrek/sastav: to-integer first first first db
    mysql_cmd [ "SELECT count(*) FROM tehnolog WHERE sifra='M-" mater_sif "' " ]
    mrek/techno: to-integer first first first db
    mysql_cmd [ "SELECT count(*) FROM tehn_roba WHERE sif_robe='M-" mater_sif "' " ]
    mrek/techno: mrek/techno + (to-integer first first first db)
    mysql_cmd [ "SELECT count(*) FROM rezervacija WHERE sif_mat='M-" mater_sif "' " ]
    mrek/rezerv: to-integer first first first db
    mysql_cmd [ "SELECT count(*) FROM nabavka WHERE sif_robe='M-" mater_sif "' " ]
    mrek/nabavka: to-integer first first first db
    mysql_cmd [ "SELECT count(*) FROM prom_potnab WHERE sif_robe='M-" mater_sif "' " ]
    mrek/nabavka: mrek/nabavka + (to-integer first first first db)
    psz: 400 pmag: 300
    c1: color_g1 c2: color_g2
    if mrek/promet > 0 [ c1: color_m1 c2: color_m2 ]
    retval: false ; ha x-elik az ablakot
    tabla_blk: compose
    [
      size (as-pair psz pmag)
      style tlab text  with [ edge: [color: coal size: 1x1 ]]
      backtile polished color_g2
      across
      at 9x9 button close_btn_img 26x26
      [
        retval: false
        hide-popup
      ]
      ;
      at 40x10  text "Šifra zamene:" 120x24
      at 160x10 arrow right 24x24 color_l2
      [
        tabla_keres "materijal2_izb"
        izbor_sifre none
        if block? retval
        [
          t_sif/text: mrek/sifra: copy retval/1
          t_naz/text: mrek/naziv: get_table_value "t1.naz_mat" retval
          show t_sif
          show t_naz
        ]
      ]
      at 200x10 t_sif: info center (mrek/sifra) 170x24 color_g2
      at 10x40  t_naz: text (mrek/naziv) 350x24
      at 10x90  text right "Promet:"      150x24 at 160x90  tlab center (int_form/blank mrek/promet 0)  60x24 (c1) (c2)
      at 10x130 text right "Sastav:"      150x24 at 160x130 tlab center (int_form/blank mrek/sastav 0)  60x24 color_g1 color_g2
      at 10x170 text right "Tehnologija:" 150x24 at 160x170 tlab center (int_form/blank mrek/techno 0)  60x24 color_g1 color_g2
      at 10x210 text right "Rezervacije:" 150x24 at 160x210 tlab center (int_form/blank mrek/rezerv 0)  60x24 color_g1 color_g2
      at 10x250 text right "Nabavka:"     150x24 at 160x250 tlab center (int_form/blank mrek/nabavka 0) 60x24 color_g1 color_g2
      ;
      at 270x90 button "Zameniti" 100x24 color_m2
      [
        hb: zamena_materijala mrek
        retval: false
        if string? hb [ alert hb exit ]
        if hb = false [ alert "Zamena nije uspela!" hide-popup exit ]
        retval: true
        rek/sif_mat: copy mrek/sifra
        ablak_cezar
        mod_mater
        hide-popup
      ]
      at 270x210 button "Brisati" 100x24 color_m4
      [
        if (request/confirm rejoin [ "Brisati materijal: " mater_sif " ?" ]) <> true [ exit ]
        hb: brisanje_materijala mrek
        retval: false
        if string? hb [ alert hb exit ]
        if hb = false [ alert "Brisanje nije uspeo!" hide-popup exit ]
        retval: true
        ablak_cezar
        hide-popup
      ]
    ]
    ablak_p: layout/offset tabla_blk 0x0
    inform/title ablak_p "brisanje materijala"
  ]
  
  set 'osvez_name_mat: func [ /local sz0 row0 tar0 rek blk blk2 s1 ]
  [
    s1: false  ; kapcsolo, ami engedelyezi az importot
    blk:  make block! [] ; hibatarolo
    blk2: make block! [] ; row0 parse
    hbszov: reduce [ make string! 10000 ] ; 
    if error? try [ tar0: read/lines request-file/only ] [ alert "Greška!" exit ]
    fp1: flash "Import.."
    if (request/confirm "Modifikovati postojeæe name materijali?") = true [ s1: true ]
    foreach row0 tar0
    [
      s1: true ; hiba eseten 'false' nem ir a bazisba kapcsolo
      blk2: parse/all row0 "|"
      mysql_cmd 
      [
        "SELECT t1.sif_mat,t1.name FROM materijal AS t1 "
        "WHERE t1.sif_mat='" blk2/1 "' "
      ]
      rek: get_rekord first db
      ;
      ;----- hibajelentes ------
      if not retval ; az aktualis materijal letezik-e a bazisban
      [ 
        append hbszov/1 rejoin [ blk2/1 " " blk2/2 " NEMA U BAZI!^/" ]
        s1: false
      ]
      ;-------------------------
      ;
      if s1   ; ha nincs hiba akkor modositom a bazisban
      [
        mysql_cmd
        [
          "UPDATE materijal SET name='" blk2/2 "' "
          "WHERE sif_mat='" blk2/1 "' "
        ]
      ]
    ]
    unview/only fp1
    nyomtatas_txt hbszov "greske_import_name_mat.txt"
  ]
  
  set 'osvez_car_tarif: func [ /local sz0 row0 tar0 rek blk blk2 s1 s2 s3 ]
  [
    s1: false ; torlom a meglevo tartalmakat a tarif_car-bol
    s2: false ; ha nem ures tarif_car, modositom e vagy sem
    s3: true  ; ha van valami hiba a sifraval vagy a tarifa szammal
    blk: make block! [] ; hibatarolo
    blk2: make block! [] ; row0 parse
    hbszov: reduce [ make string! 10000 ] ; 
    if error? try [ tar0: read/lines request-file/only ] [ alert "Greška!" exit ]
    fp1: flash "Import.."
    if (request/confirm "Brisati postojeæe tarifne brojeve?") = true [ s1: true ]
    if (request/confirm "Modifikovati postojeæe tarifne brojeve?") = true [ s2: true ]
    if s1 
    [
      mysql_cmd
      [
        "UPDATE materijal SET tarif_car='' WHERE 1 "
      ]
    ]
    foreach row0 tar0
    [
      s3: true ; hiba eseten 'false' nem ir a bazisba kapcsolo
      blk2: parse row0 "|"
      mysql_cmd 
      [
        "SELECT t1.sif_mat,t1.tarif_car FROM materijal AS t1 "
        "WHERE t1.sif_mat='" blk2/1 "' "
      ]
      rek: get_rekord first db
      ;
      ;----- hibajelentes ------
      if not retval ; az aktualis materijal letezik-e a bazisban
      [ 
        append hbszov/1 rejoin [ blk2/1 " " blk2/2 " NEMA U BAZI!^/" ]
        s3: false
      ]
      if s2 and (rek/tarif_car = "") ; Tarif_car nem lehet ures
      [
        append hbszov/1 rejoin [ blk2/1 " " blk2/2 " Tarif_car je prazna!^/" ]
        s3: false
      ]
      ;-------------------------
      ;
      if s3   ; ha nincs hiba akkor modositom a bazisban
      [
        if s2 and (rek/tarif_car <> "") ; ha kivalasztom hogy modositsa a nem ures tarif_car mezoket
        [
          mysql_cmd
          [
            "UPDATE materijal SET tarif_car='" blk2/2 "' "
            "WHERE sif_mat='" blk2/1 "' "
          ]
        ]
        if not s2 and (rek/tarif_car = "") ; abban az esetben fut le ha minden sorban frissiteni kell, mindent ahol ures a tarif_car
        [
          mysql_cmd
          [
            "UPDATE materijal SET tarif_car='" blk2/2 "' "
            "WHERE sif_mat='" blk2/1 "' "
          ]
        ] 
      ]
    ]
    unview/only fp1
    nyomtatas_txt hbszov "greske_import_tarif_car.txt"      
  ]
  
  auto_naziv_osszerako_adat_kiszed: func [ sz0[string!] /filter /local ]
  [
    either filter
    [
      mysql_select/2: copy "t1.*"
      mysql_select/8: copy " ORDER BY t1.sif_mat ASC "
      mysql_cmd mysql_select
      return get_rekordset copy db
    ]
    [
      mysql_cmd
      [
        "SELECT * FROM " tname " WHERE " sz0 " ORDER BY sif_mat ASC "
      ]
      return get_rekordset copy db
    ]
  ]
  
  automata_naziv_osszerako: func [ /local tar0 row0 blk poz0 sz0 naz1 naz2 naz3 frm1 frm2 frm3 fp1 oblik1 trg_naz ]
  [
    trg_naz: copy ""
    oblik1: request-text/title {Upisati šifra oblika ili "-" za filter ili xx za sve: }
    if not string? oblik1 [ exit ]
    either oblik1 = "xx" 
    [
      sz0: rejoin [ "(bris=" apjel "A" apjel " OR bris=" apjel "B" apjel " OR bris=" apjel "D" apjel ")" ]
    ]
    [
      sz0: rejoin [ "oblik=" apjel oblik1 apjel " AND (bris=" apjel "A" apjel " OR bris=" apjel "B" apjel " OR bris=" apjel "D" apjel ")" ]
    ]
    hbszov: reduce [ make string! 100000 ]
    fp1: flash "Uèitavanje podataka..."
    either oblik1 = "-"
    [
      tar0: auto_naziv_osszerako_adat_kiszed/filter ""
    ]
    [
      tar0: auto_naziv_osszerako_adat_kiszed sz0
    ]
    unview/only fp1
    fp1: flash "Obrada naziva..."
    foreach row0 tar0
    [
      mater_sif: copy row0/sif_mat
      rek: row0
      filter_kreal
      forma_gyujto
      naz1: copy ""
      naz2: copy ""
      naz3: copy ""
      either not string? frm1: select formak_blk "sr"
      [
        append hbszov/1 rejoin [ str_form mater_sif 18 " : Greška: Fali forma za naziv!^/" ]
      ]
      [
        either (row0/oznaka = "K") and (row0/naziv2 <> "")  and (row0/tip <> "R")
        [
          naz1: copy row0/naziv2
        ]
        [
          naz1: nevszerkeszto "sr" frm1
        ]
        ;
        if string? frm2: select formak_blk "hu"
        [
          naz2: nevszerkeszto "hu" frm2
        ]
        if string? frm3: select formak_blk "en"
        [
          naz3: nevszerkeszto "en" frm3
        ]
        blk: make block! []
        poz0: head formak_blk
        while [ not tail? poz0 ]
        [
          if find poz0/1 "."
          [
            naz0: nevszerkeszto poz0/1 poz0/2
            if naz0 <> ""
            [
              if poz0/1 = "sr.car" [ trg_naz: naz0 ]
              append blk poz0/1
              append blk naz0
            ]
          ]
          poz0: next next poz0
        ]
        sz0: blokk_pakolo blk "|"
        if (row0/naziv_list <> sz0) or (row0/naz_mat <> naz1)
        [
          replace/all sz0 "#n" "\n"
          mysql_cmd
          [
            "UPDATE " tname " SET naziv_list='" sz0 "',naz_mat='" naz1 "',trgov_naz='" trg_naz "',"
            "kor_modif='" korisnik "',dat_modif=SYSDATE() "
            "WHERE sif_mat = '" mater_sif "' "
          ]
          if not mysts [ alert "Greška kod ispisa podataka!" exit ]
          append hbszov/1 rejoin [ str_form mater_sif 18 " : " naz1 "^/" ]
        ]
        ;
        if ((row0/trans_naz <> naz2) and (naz2 <> ""))
        [
          naz2: copy ""
          append hbszov/1 rejoin [ str_form mater_sif 18 " : " naz1 "^/" ]
        ]
        ;
        if ((row0/name <> naz3) and (naz3 <> "") and (row0/ino_naz <> ""))
        [
          naz3: copy ""
          if not mysts [ alert "Greška kod ispisa podataka!" exit ]    
          append hbszov/1 rejoin [ str_form mater_sif 18 " : " naz1 "^/" ]
        ]
        mysql_cmd
        [
          "UPDATE " tname " SET trans_naz='" naz2 "',name='" naz3 "',"
          "kor_modif='" korisnik "',dat_modif=SYSDATE() "
          "WHERE sif_mat = '" mater_sif "' "
        ]
        if not mysts [ alert "Greška kod ispisa podataka!" exit ]
      ]
    ]
    unview/only fp1
    nyomtatas_txt hbszov rejoin [ "promena_naziva_mat.txt"]
  ]
  ;
  clear_keres_rek: func []
  [
    keres_rek: make object! compose
    [
      nazdeo:   (copy "")
      standard: (copy "")
      katbr1:   (copy "")
      proizv:   (copy "")
      magacin:  (copy "-")
      bris:     (copy "")
    ]
  ]
  
  klas_naziv_keres: func [ naz1[block!] /local poz0 row0 rbr1 mbr1 row1 val1 ]
  [
    ; keres definialt valtozot:
    if string? val1: select valtoz_blk naz1/2 [ return val1 ]
    ; keres definialt nazivot:
    foreach [ poz0 row0 ] filter_blk
    [
      if row0/sifra = naz1/2
      [
        if row0/tip <> "a" [ return copy row0/value ]
        foreach [ rbr1 mbr1 ] row0/sastav
        [
          row1: select klas_tar mbr1
          if object? row1
          [
            if row1/sifra = row0/value
            [
              if string? val1: select row1/naz_list naz1/1 [ return val1 ]
            ]
          ]
        ]
        return none
      ]
    ]
    return none
  ]
  
  ertek_atadas: func [ row0[block!] /local naz0 poz0 row1  ]
  [
    naz0: parse/all row0/3 "."
    if (length? naz0) < 2 [ insert naz0 "sr" ]
    row1: none
    poz0: head filter_blk
    while [ not tail? poz0 ]
    [
      if poz0/2/sifra = row0/1 [ row1: poz0/2 break ]
      poz0: next next poz0
    ]
    if not object? row1 [ exit ]
    if not string? val1: klas_naziv_keres naz0 [ exit ]
    row1/value: val1 
  ]
  
  ; uj valtozot definial 
  ertek_definialas: func [ row0[block!] /local sz0 sz1 i1 poz1 poz2 ]
  [
    if none? sz0: select valtoz_blk row0/1 [ exit ]
    sz1: copy ""
    i1: 0
    poz2: at row0 3
    while [ not tail? poz2 ]
    [
      poz1: head filter_blk
      while [ not tail? poz1 ]
      [
        if poz1/2/sifra = poz2/1
        [
          if poz1/2/value <> "" 
          [
            i1: i1 + 1
            switch/default i1
            [
              1 [ append sz1 rejoin [ "[ " poz1/2/value " ]" ] ]
              2 [ append sz1 rejoin [ " " poz1/2/value ] ]
            ]   [ append sz1 rejoin [ " - " poz1/2/value ] ]
          ]
        ]
        poz1: next next poz1
      ]
      poz2: next poz2
    ]
    change sz0 sz1
  ]
  ;
  forma_gyujto: func [/local poz0 poz1 poz2 row0 row1 row2 sz0 sz1 rbr1 mbr1 ]
  [
    ;jed_mere: copy ""
    formak_blk: make block! []
    valtoz_blk: parse "x1,,x2,,x3,," none
    novi_sif: copy ""
    ; kategoriakat tartalmazo lista
    foreach [ poz0 row0 ] filter_blk
    [
      if row0/nivo <> "b"
      [
        if row0/value > "-"
        [
          foreach [ rbr1 mbr1 ] row0/sastav
          [
            row1: select klas_tar mbr1
            if object? row1
            [
              if row1/sifra = row0/value 
              [
                if row1/poc_sifre <> "" [ novi_sif: copy row1/poc_sifre ]
                if row1/jed_mere <> "" [ jed_mere: copy row1/jed_mere ]
                ; --- ertek-atadas:
                foreach row2 row1/pdci_list
                [
                  row2: parse row2 none
                  if (length? row2) >= 3
                  [
                    if row2/2 = "=" [ ertek_atadas row2 ]
                    if row2/2 = ":" [ ertek_definialas row2 ]
                  ]
                ]
                ; formak gyujtese:
                poz1: head row1/frm_list
                while [ not tail? poz1 ]
                [
                  if none? poz2: find/skip formak_blk poz1/1 2
                  [
                    poz2: tail formak_blk
                    append formak_blk poz1/1
                    append formak_blk none
                  ]
                  poz2/2: copy poz1/2
                  poz1: next next poz1
                ]
              ]
            ]
          ]
        ]
      ]
    ]
  ]
  
  kontrola_uslova: func [ row1[object!] /local f1 f2 poz0 poz3 row0 row2 row3 ]
  [
    ; uslovi:
    f1: true
    f2: false
    foreach row2 row1/usl_list
    [
      row2: parse row2 none
      if (length? row2) = 3
      [
        foreach [ poz0 row0 ] filter_blk
        [
          if row2/1 = row0/sifra
          [
            ; ha a feltetel: sif1 (=,#) sif2
            foreach [ poz3 row3 ] filter_blk
            [
              if row2/3 = row3/sifra
              [
                f2: true
                either row2/2 = "="
                [
                  if (row3/value <> row0/value) or (row3/value = "") [ f1: false ]
                ]
                [
                  if (row3/value = row0/value) [ f1: false ]
                ]
              ]
            ]
            if f2 = false
            [
              either row2/2 = "="
              [
                if row0/value <> row2/3 [ f1: false ]
              ]
              [
                if row0/value = row2/3 [ f1: false ]
              ]
            ]
          ]
        ]
      ]
    ]
    return f1
  ]
  ; a materijal nevet hatarozo parameterek
  ; filter_blk mar be van allitva
  parameter_filter: func [ /local poz0 row0 poz1 poz2 row1 p1 p2 n1 i1 sif1 rbr1 mbr1 sz1 t_btn hb1 af ]
  [
    tabla_blk: compose
    [
      size p1
      backdrop color_g4
      across
    ]
    ;
    p1: 10x60
    ; toltes: formak_blk
    plan_naziv: copy ""
    forma_gyujto  
    ; kategoriakat tartalmazo lista
    foreach [ poz0 row0 ] filter_blk
    [
      if row0/nivo <> "b"
      [
        row1: parse poz0 "."
        p2: as-pair ((length? row1) * 6 - 16) 0
        sz1: rejoin [ row0/obav " " row0/naziv ":" ]
        append tabla_blk compose
        [
          at (p1 + p2) text (sz1) 190x24
        ]
        ; alapertelmezett ertek:
        if empty? row0/pdci_list [ append row0/pdci_list copy "" ]
        either row0/tip <> "a"
        [
          if row0/value = "" [ row0/value: copy row0/pdci_list/1 ]
          append tabla_blk compose/only
          [
            at (p1 + 190x0 + p2) field (row0/value) 320x24
            with (compose/only 
            [ 
              user-data: (reduce [ "bemezo" row0 ]) 
            ])
            [
              row0: face/user-data/2
              foreach [ rbr1 mbr1 ] row0/sastav
              [
                row1: select klas_tar mbr1
                if object? row1
                [
                  ;if row1/sifra = row0/value
                  ;[
                  ; a legelson lemegy
                    sif1: join row0/poz "."
                    filter_torol sif1
                    filter_bovit row1 sif1
                    sort/skip filter_blk 2
                    break
                  ;]
                ]
              ]
              akt_fld: row0/sifra
              parameter_filter
            ]
          ]
        ]
        [
          ; leszalado lista toltese:
          row0/lista: copy/deep [ [] [] ]
          foreach [ rbr1 mbr1 ] row0/sastav
          [
            ; vrednost tipusuak:
            row1: select klas_tar mbr1
            if object? row1
            [
              if kontrola_uslova row1
              [
                append row0/lista/1 row1/sifra
                append row0/lista/2 row1/naziv
              ]
            ]
          ]
          if not find row0/lista/1 "-"
          [
            insert row0/lista/1 "-"
            insert row0/lista/2 ""
          ]
          if row0/value = "" [ row0/value: copy row0/pdci_list/1 ]
          if not find row0/lista/1 row0/value [ row0/value: copy "" ]
          if row0/value = ""
          [
            ;ha van ures opcio, akkor annak a sifraja:
            row0/value: blk_find row0/lista/2 row0/lista/1 ""
          ]
          n1: min max (length? row0/lista/2) 4 15
          append tabla_blk compose/only
          [
            at (p1 + 190x0 + p2) drop-down data (row0/lista/2) 320x24
            with (compose/only 
            [
              rows: (n1) 
              text: (blk_find row0/lista/1 row0/lista/2 row0/value) 
              user-data: (reduce [ "belista" row0 ])
            ])
            [
              if string? face/data
              [
                row0: face/user-data/2
                row0/value: blk_find row0/lista/2 row0/lista/1 face/text
                foreach [ rbr1 mbr1 ] row0/sastav
                [
                  row1: select klas_tar mbr1
                  if object? row1
                  [
                    if row1/sifra = row0/value
                    [
                      sif1: join row0/poz "."
                      filter_torol sif1
                      filter_bovit row1 sif1
                      sort/skip filter_blk 2
                      break
                    ]
                  ]
                ]
                akt_fld: none
                parameter_filter
              ]
            ]
          ]
        ]
        p1: p1 + 0x35
      ]
    ]
    ;
    poz2: head formak_blk
    while [ not tail? poz2 ]
    [
      if poz2/1 = "sr"
      [
        plan_naziv: nevszerkeszto poz2/1 poz2/2
        break
      ]
      poz2: next next poz2
    ]
    append tabla_blk compose
    [
      at 10x10 info wrap left (plan_naziv) 570x40 color_g2
    ]
    ; forme
    if find [ "prg" "adm" ] dozvola
    [
      append tabla_blk compose
      [
        at (p1) text right "Forme:" 80x24
      ]
      poz1: head formak_blk
      i1: 0
      while [ not tail? poz1 ]
      [
        sz1: nevszerkeszto poz1/1 poz1/2
        i1: i1 + 1
        append tabla_blk compose
        [
          at (p1 +  80x0)  info (poz1/1)  60x24 color_g2
          at (p1 + 140x0)  info (poz1/2) 350x24 color_g2
          at (p1 + 140x25) info (sz1)    430x24 color_g2
        ]
        p1: p1 + 0x55
        i1: i1 + 1
        poz1: next next poz1
      ]
      if i1 = 0 [ p1: p1 + 0x30 ]
      append tabla_blk compose
      [
        at (p1) text right "Vars:" 80x24
      ]
      poz1: valtoz_blk
      i1: 0
      while [ not tail? poz1 ]
      [
        if poz1/2 <> ""
        [
          append tabla_blk compose
          [
            at (p1 +  80x0) info (poz1/1)  60x24 color_g2
            at (p1 + 140x0) info (poz1/2) 350x24 color_g2
          ]
          p1: p1 + 0x30
          i1: i1 + 1
        ]
        poz1: next next poz1
      ]
      if i1 = 0 [ p1: p1 + 0x30 ]
    ]
    ; adatok kontrollja:
    hb1: none
    foreach [ poz0 row0 ] filter_blk
    [
      if row0/naziv = ""
      [
        hb1: "Fali naziv polja!" 
        break 
      ]
      if (row0/nivo <> "b") and (row0/obav = "*")
      [
        if row0/value = "" [ hb1: rejoin [ "Upiši podatak: " row0/naziv "!" ] break ]
        ;
        if find [ "dim_a" "dim_b" "dim_c" ] row0/sifra
        [
          if not none? find row0/value "-"
          [
            hb1: rejoin [ "Pravilno upiši podatak: " row0/naziv "!" ] break
          ]
        ]
      ]
    ]
    either string? hb1
    [
      append tabla_blk compose
      [
        at (p1 + 80x0) h3 (hb1) 300x24 
      ]
    ]
    [
      either mater_sif = ""
      [
        append tabla_blk compose
        [
          at (p1 + 190x0) t_btn: button "Upis novog" 120x24 color_m2
          [
            if (length? plan_naziv) > 200 [ alert "Naziv materijala max.200 karakt.!" exit ]
            upis_novog
          ]
        ]
      ]
      [
        append tabla_blk compose
        [
          at (p1 + 190x0) t_btn: button "Promeniti" 120x24 color_m2
          [
            if (length? plan_naziv) > 200 [ alert "Naziv materijala max.200 karakt.!" exit ]
            modif_param
          ]
        ]
      ]
    ]
    p1: max (p1 + 0x50) ablak_q/size
    ablak_q/pane: layout/offset tabla_blk ablak_q/user-data
    either mater_sif = ""
    [
      ablak_q/size: p1
      ablak_c/pane/size: ablak_q/pane/size + 0x160
      show ablak_c
    ]
    [
      show ablak_q
    ]
    mat_tablazat
    ; kotelezo mezo keresese:
    poz1: head ablak_q/pane/pane
    while [ not tail? poz1 ]
    [
      if block? row1: get in poz1/1 'user-data
      [
        if row1/1 = "bemezo"
        [
          row0: row1/2
          if (row0/obav = "*") and (row0/value = "") 
          [ 
            akt_fld: row0/sifra
            focus poz1/1 exit 
          ]
        ]
      ]
      poz1: next poz1
    ]
    ; aktualis utani mezo kereses: 
    af: none
    poz1: head ablak_q/pane/pane
    while [ not tail? poz1 ]
    [
      if block? row1: get in poz1/1 'user-data
      [
        if row1/1 = "bemezo"
        [
          row0: row1/2
          if none? af [ af: poz1/1 ]
          either row0/sifra = akt_fld [ akt_fld: none ]
          [
            if none? akt_fld [ af: poz1/1 break ]
          ]
        ]
      ]
      poz1: next poz1
    ]
    ; aktualis jelolese:
    either none? af [ akt_fld: none ]
    [
      row1: get in af 'user-data
      row0: row1/2
      akt_fld: row0/sifra
      focus af
    ]
  ]
  
  parameter_ablak: func [ ]
  [
    tabla_blk: compose
    [
      size ablak_c/size
      backtile polished color_g2
      at 9x9 button close_btn_img 26x26
      [
        hide-popup
      ]
      at 40x10 h3 "Promena parametara" 300x24      
      at 0x40 ablak_q: box color_g4 (ablak_c/size - 0x40) with [  edge: [color: coal size: 1x1 ] ]
      at (as-pair (ablak_c/size/1 - 20) 40) slider (as-pair 20 (ablak_c/size/2 - 40)) with [ user-data: ablak_q ]
      [ scroller_pozicio face ]
    ]
    ablak_p: layout/offset tabla_blk 0x0
    ablak_q/user-data: 0x0
    ablak_p/user-data: ablak_q ; gorgetheto-objektum
    parameter_filter
    ;
    inform/title ablak_p "Parametri materijala"
  ]
    
  param_kontrola: func [ sz1[string!] /local sz2 ]
  [
    sz2: copy sz1
    sz2: replace/all sz2 "," " AND "
    mysql_cmd [ "SELECT * FROM " tname " WHERE " sz2 " sif_mat<>'" mater_sif "' " ]
    if not empty? row: first db [ return false ]
    return true
  ]
  ;
  ujsifra_ablak: func [ sr_naz[string!] /local psz pmag poc_sif zad_sif ]
  [
    poc_sif: copy ""
    if novi_sif <> "" [ poc_sif: copy novi_sif ]
    zad_sif: copy ""
    psz: 640 pmag: 180
    retval: none
    tabla_blk: compose
    [
      size (as-pair psz pmag)
      backtile polished color_g4
      across space 0x0
      at 9x9 button close_btn_img 26x26
      [
        jed_mere: copy ""
        hide-popup
      ]
      at 50x30 text "Možete da unesete poèetak šifre (2 broja) ili celu šifru (XX-XXXX):" 500X24
      at 20x70 text right "Šifra:" 60x24
      at 90x70 field poc_sif 60x24 h2 center " - " 25x24
      at 170x70 field zad_sif 150x24
      at 330x70 text right "JM:" 40x24
      at 370x70 drop-down data jm_lista 80x24
      with [ rows: 4 text: jed_mere ]
      [
        if string? face/data
        [
          jed_mere: copy face/text
        ]
      ]
      at 500x70 button "Upis" 80x24 color_m2
      [
        if jed_mere = "" [ alert "Fali jed.mere!" exit ]
        if (length? poc_sif) <> 2
        [
          alert "Upisati poèetak šifre (2 broja)!" exit
        ]
        retval: rejoin [ poc_sif "-" zad_sif ]
        hide-popup
      ]
      at 50x120 h2 sr_naz 500x24
    ]
    ablak_p1: layout/offset tabla_blk 0x0
    inform/title ablak_p1 "Šifra alata"
  ]
  ;
  upis_novog: func [ /local poz0 row0 row1 sz0 sz1 sif1 n1 t1 frm1 frm2 frm3 naz0 naz1 naz2 naz3 blk sif2 sif_blk n2 trg_naz ]
  [
    naz1: copy ""
    naz2: copy ""
    naz3: copy ""
    trg_naz: copy ""
    if not string? frm1: select formak_blk "sr"
    [
      alert "Fali forma za naziv!" exit
    ]
    naz1: nevszerkeszto "sr" frm1
    if string? frm2: select formak_blk "hu"
    [
      naz2: nevszerkeszto "hu" frm2
    ]
    if string? frm3: select formak_blk "en"
    [
      naz3: nevszerkeszto "en" frm3
    ]
    blk: make block! []
    poz0: head formak_blk
    while [ not tail? poz0 ]
    [
      if find poz0/1 "."
      [
        naz0: nevszerkeszto poz0/1 poz0/2
        if naz0 <> ""
        [
          if poz0/1 = "sr.car" [ trg_naz: naz0 ]
          append blk poz0/1
          append blk naz0
        ]
      ]
      poz0: next next poz0
    ]
    ; beiras:
    sz1: sif1: copy ""
    n1: 0
    t1: 0
    foreach [ poz0 row0 ] filter_blk
    [
      if find g_baze_sif row0/sifra
      [
        append sz1 rejoin [ row0/sifra "=" apjel row0/value apjel "," ]
        if (row0/sifra = "ino_naz") and (row0/value = "") [ naz3: copy "" ]
      ]
    ]
    ; egyedi anyag ellenorzese
    if not param_kontrola sz1 [ alert "Materijal sa takvim parametrima veæ postoji!" exit ]
    ;
    if none? naz1 [ naz1: copy "" ]
    ujsifra_ablak naz1
    sif2: copy ""
    if none? retval [ exit ]
    if not string? sif1 [ exit ]
    sif1: trim uppercase copy retval
    if not jo_sifra_jelek sif1 [ alert "Šifra sadrži nedozvoljeno slovo!" exit ]
    sif_blk: parse/all sif1 "-"
    either none? sif_blk/2
    [
      n1: length? sif_blk/1
      if (n1 <> 2) [ alert "Poèetak šifre 2 broja!" exit ]
      mysql_cmd
      [
        "SELECT MAX(MID(sif_mat," (n1 + 2) ")) FROM " tname " WHERE sif_mat LIKE '" sif_blk/1 "-%' "
      ]
      if empty? row1: first db [ alert "Poèetak šifre upisati ruèno!"  exit ]
      if none? row1/1/1 [ alert "Poèetak šifre upisati ruèno!"  exit ]
      sif2: rejoin [ sif_blk/1 "-" kovetkezo_sifra row1/1/1 ]
    ]
    [
      n2: length? sif_blk/2
      if (n2 < 4) [ alert "Drugi deo šifre min. 4 broja!" exit ]
      ; kihuzva 2017-01-09 Laci
      ; if not parse sif_blk/2 [ 4 digits ] [ alert "Drugi deo šifre 4 broja!" exit ] 
      sif2: rejoin [ sif_blk/1 "-" sif_blk/2 ]
    ]
    ;if (length? sif2) <> 7 [ alert "Šifra 2 + 4 slova!" exit ]
    mysql_cmd [ "SELECT sif_mat FROM " tname " WHERE sif_mat='" sif2 "'" ]
    if not empty? row: first db [ alert "Šifra veæ postoji!" exit ]
    ;
    sz0: blokk_pakolo blk "|"
    replace/all sz0 "#n" "\n"
    mysql_cmd
    [
      "INSERT INTO " tname " SET " sz1
      "sif_mat='" sif2 "',naz_mat='" naz1 "',"
      "trans_naz='" naz2 "',name='" naz3 "',trgov_naz='" trg_naz "',"
      "naziv_list='" sz0 "',bris='A',jed_mere='" jed_mere "',oblik_car='',"
      "kor_insert='" korisnik "',dat_insert=CURDATE(),dat_modif=CURDATE() "
    ]
    if not mysts [ alert "Greška kod ispisa podataka!" exit ]
    mater_sif: copy sif2
    jed_mere:   copy ""
    mysql_cmd
    [
      "SELECT t1.*,IFNULL((SELECT t2.naziv FROM vrste_sirov AS t2 WHERE t2.vrsta=t1.vrsta_sir),'') AS naziv_sir "
      "FROM " tname " AS t1 WHERE t1.sif_mat='" mater_sif "' "
    ]
    rek: get_rekord first db
    if not retval
    [
      alert ~"Materijal nije u bazi!" 
      mater_sif: copy "" 
      exit
    ]
    nevlistabol_bazis_friss "oz1" "katbr4" [ "podgr_mat" "sir_mat" ]
    filter_tab: clear_filter
    filter_kreal ; rekord filter beallitasa
    rek/naziv_list: parse/all rek/naziv_list "|"
    mod_mater
    hide-popup
    mat_tablazat 
  ]
  ;
  modif_param: func [ /local naz1 naz3 sz1 frm1 frm3 blk poz0 sz0 naz0 trg_naz ]
  [
    nevlistabol_bazis_friss "oz1" "katbr4" [ "podgr_mat" "sir_mat" ]
    naz1: sz1: copy ""
    trg_naz: copy ""
    if not string? frm1: select formak_blk "sr"
    [
      alert "Fali forma za naziv!" exit
    ]
    naz1: nevszerkeszto "sr" frm1
    if string? frm3: select formak_blk "en"
    [
      naz3: nevszerkeszto "en" frm3
      if rek/ino_naz <> ""
      [
        append sz1 rejoin [ "name=" apjel naz3 apjel "," ]
      ]
    ]
    blk: make block! []
    poz0: head formak_blk
    while [ not tail? poz0 ]
    [
      if find poz0/1 "."
      [
        naz0: nevszerkeszto poz0/1 poz0/2
        if naz0 <> ""
        [
          if poz0/1 = "sr.car" [ trg_naz: naz0 ]
          append blk poz0/1
          append blk naz0
        ]
      ]
      poz0: next next poz0
    ]
    foreach [ poz0 row0 ] filter_blk
    [
      ;if row0/value <> "" kiveve 2017-01-20 Laci
      ;[
        append sz1 rejoin [ row0/sifra "=" apjel row0/value apjel "," ]
      ;]
    ]
    ; ellenorzes
    if not param_kontrola sz1 [ alert "Materijal sa takvim parametrima veæ postoji!" exit ]
    ;
    sz0: blokk_pakolo blk "|"
    replace/all sz0 "#n" "\n"
    mysql_cmd
    [
      "UPDATE " tname " SET " sz1
      "naz_mat='" naz1 "',trgov_naz='" trg_naz "',naziv_list='" sz0 "',kor_modif='" korisnik "',dat_modif=CURDATE() "
      "WHERE sif_mat='" mater_sif "' "
    ]
    if not mysts [ alert "Greška kod ispisa podataka!" exit ]
    mysql_cmd
    [
      "SELECT t1.*,IFNULL((SELECT t2.naziv FROM vrste_sirov AS t2 WHERE t2.vrsta=t1.vrsta_sir),'') AS naziv_sir "
      "FROM " tname " AS t1 WHERE t1.sif_mat='" mater_sif "' "
    ]
    rek: get_rekord first db
    if not retval
    [
      alert ~"Materijal nije u bazi!" 
      mater_sif: copy "" 
      exit
    ]
    filter_tab: clear_filter
    filter_kreal ; rekord filter beallitasa
    rek/naziv_list: parse/all rek/naziv_list "|"
    mod_mater
    hide-popup
    mat_tablazat 
  ]
  ;
  sirovina_frissit: func [ /local tar0 tar1 row0 row1 poz0 sz1 sz2 blk naz1 n1 ]
  [
    n1: 0
    naz1: sz1: sz2: copy ""
    mysql_cmd
    [
      "SELECT * FROM " tname " WHERE tip='R' ORDER BY sif_mat ASC"
    ]
    tar1: get_rekordset copy db 
    foreach row1 tar1
    [
      mater_sif: copy row1/sif_mat
      rek: row1
      filter_kreal
      foreach [ poz0 row0 ] filter_blk
      [
        if (row0/sifra = "podgr_mat") or (row0/sifra = "sir_mat")
        [
          mysql_cmd
          [
            "SELECT naz_list FROM klasif_robe WHERE klasa='" row0/mbr "' AND ozn='2' "
            "AND (sifra='" rek/podgr_mat "' OR sifra='" rek/sir_mat "') LIMIT 1"
          ]
          blk: get_rekord first db
          if not none? blk/naz_list
          [
            naz1: parse/all blk/naz_list "|"
            sz2: select naz1 "oz1"
            if not none? sz2
            [
              n1: n1 + 1
              mysql_cmd
              [
                "UPDATE materijal SET katbr4='" sz2 "' WHERE sif_mat='" rek/sif_mat "' "
              ]
            ]
          ]
        ]
      ]
    ]
    alert rejoin [ "updated " n1 " materijal" ]
  ]
  ;
  nevlistabol_bazis_friss: func [ nev1[string!] mezo1[string!] sif_blk[block!] /local poz0 row0 row1 mbr1 rbr1 val1 sif1 ]
  [
    foreach sif1 sif_blk
    [
      foreach [ poz0 row0 ] filter_blk
      [
        if row0/sifra = sif1
        [
          foreach [ rbr1 mbr1 ] row0/sastav
          [
            row1: select klas_tar mbr1
            if object? row1
            [
              if row1/sifra = row0/value
              [
                val1: select row1/naz_list nev1
                if not none? val1
                [
                  mysql_cmd
                  [
                    "UPDATE " tname " SET " mezo1 "='" val1 "' "
                    "WHERE sif_mat='" rek/sif_mat "'"
                  ]
                ]
                break
              ]
            ]
          ]
          break
        ]
      ]
    ]
  ]
  ;
  ; sif1 = sr,hu,en,sr.prop,..
  ; frm1 = forma leiras
  nevszerkeszto: func [ sif1[string!] frm1[string!] /local blk poz0 row0 row1 rbr1 mbr1 poz1 sz1 sz2 val1 f1 klist naz1 ]
  [
    klist: parse sif1 "."
    blk: parse/all frm1 "{}"  ;  *...* , "..." , (...) , 
    poz1: head blk
    sz1: copy ""
    sz2: copy ""
    either frm1/1 = #"{" [ f1: 1 ][ f1: 0 ]
    while [ not tail? poz1 ]
    [
      f1: 1 - f1 
      either f1 = 1 ; 1-es az baziselem 
      [
        trim poz1/1
        naz1: parse/all poz1/1 "."
        ; ha van elotte 'n.' akkor majd a teljes nazivot kell a nevbe illeszteni
        if (length? naz1) < 2 [ insert naz1 klist/1 ]
        ;
        if string? val1: klas_naziv_keres naz1
        [
          ; ha talalok bazistartalmat:
          if (val1 <> "") and (val1 <> "0")
          [
            append sz1 rejoin [ sz2 val1 ]
            sz2: copy ""
          ]
        ]
        ; csak az ebben az esetben jelenjen meg a kotojel a nazivban ( if find sz2 #"-" )
        if sz2 = " - "
        [
          append sz1 sz2
          sz2: copy ""
        ]
        ; ne tegye a nazivba ha ureshelyek nelkul van kotojel a kapcsos zarojelek kozott
        if (trim sz2) = "-"
        [
          sz2: copy ""
        ]
      ]
      [
        ; fix-elem
        ; ha elvalaszo-resz
        sz2: copy poz1/1
      ]
      poz1: next poz1
    ]
    ; cserelni a dupla elvalasztot:
    replace sz1 " - , " " - "
    if (copy/part sz1 2) = ", " [ remove/part sz1 2 ]
    poz1: at tail sz1 -3
    if (copy/part poz1 3) = " - " [ clear poz1 ]
    ; trimmelni:
    trim sz1
    return sz1
  ]
    
  materijal_kiir: func [ ]
  [
    mysql_cmd
    [
      "UPDATE " tname " SET naz_mat='" rek/naz_mat "',naziv2='" rek/naziv2 "',name='" rek/name "',name_hu='" rek/name_hu "',bris='" rek/bris "',"
      "faza_naz='" rek/faza_naz "',trans_naz='" rek/trans_naz "',trgov_naz='" rek/trgov_naz "',pl_cena='" dec_value rek/pl_cena "',"
      "mat_grupa='"rek/mat_grupa"',zad_cena='" rek/zad_cena "',dat_cene='" rek/dat_cene "',sif_grupe='" rek/sif_grupe "',oznaka='" rek/oznaka "',"
      "valuta='" rek/valuta "',e_cen='" dec_value rek/e_cen "',namena='" rek/namena "',jed_mere='" rek/jed_mere "',predv_mag='" rek/predv_mag "',"
      "tarif='" rek/tarif "',tarif_car='" rek/tarif_car "',inf_jed='" rek/inf_jed "',inf_koef='" rek/inf_koef "',jtez_lock='" rek/jtez_lock "',"
      "boja='" rek/boja "',vrsta_sir='" rek/vrsta_sir "',dimenzija='" rek/dimenzija "',katbr1='" rek/katbr1 "',tezina='" rek/tezina "',"
      "kor_modif='" korisnik "',dat_modif=SYSDATE() "
      "WHERE sif_mat='" rek/sif_mat "'"
    ]
    return mysts
  ]
  
  mod_mater: func [ /local poz0 row0 row1 mp_rek p1 mod_cen mod_ozn sif1 sz1 sz2 y1 kat_blk i1 c1 br_crt t1 blk0 blk1 poz1 mak_tar msif mrow ]
  [
  	blk0: make block! []
    jed_mere:  copy ""
    y1: 0
    mysql_cmd
    [
      "SELECT zad_cena,prod_cena,dat_cene,kor_cene FROM robamp "
      "WHERE sif_mat='" rek/sif_mat "' AND oznaka='' "
    ]
    mp_rek: get_rekord first db
    ;
    mysql_cmd
    [
      "SELECT * FROM katal_pod WHERE sif_robe='M-" rek/sif_mat "' ORDER BY rbr ASC "
    ]
    tar1: get_rekordset copy db
    kat_blk: array 4
    foreach row1 tar1
    [
      i1: int_value row1/rbr
      if (i1 >= 1) and (i1 <= 4) [ kat_blk/(i1): row1 ]
    ]
    ; itt vizsgalja hogy van e dokumentum betoltve a crtezhez
    mysql_cmd
    [
      "SELECT (SELECT COUNT(*) FROM " firma/data ".arhiva_trg WHERE sif_robe='M-" rek/sif_mat "')  AS broj1, "
      "(SELECT COUNT(*) FROM " firma/data ".arhiva_web WHERE sif_robe='M-" rek/sif_mat "')  AS broj2 "
    ]
    row1: get_rekord first db
    br_crt: (int_value row1/broj1) + (int_value row1/broj2)
    ;------------------------------------------
    p1: 0x0
    filter_blk: filter_rek
    tabla_blk: compose/only
    [
      size p1
      style t_nev text right 90x24
      style t_fld field 120x24
      style tlab  text with [ edge: [color: coal size: 1x1 ]]
      backtile polished color_f4
      across space -1x-1
      at 9x9 button close_btn_img 26x26
      [
        ablak_cezar
      ]
      [
        mod_mater
      ]
      at (p1 +  50x10) text right ~"Šifra:" 60x24
      at (p1 + 110x10) info rek/sif_mat    190x24 color_g2
      at (p1 + 320x10) button "Kopirati"    90x24 color_m2
      [
        filter_tab: make block! []
        foreach [ poz0 row0 ] filter_rek
        [
          append filter_tab copy poz0
          append filter_tab construct/with [] row0
        ]
        jed_mere: copy rek/jed_mere
        ablak_cezar
      ]
      at (p1 + 0x40) text right ~"Naziv:"  110x24
      at (p1 + 0x85) text right ~"Naziv2:" 110x24
    ]
    ;-- figyelem h le van e zarva a naziv modositas
    either rek/faza_naz = "Z"
    [
    	append tabla_blk compose
      [
        at (p1 + 110x190) info wrap rek/trans_naz 490x40 color_g2
        at (p1 + 110x235) w_field rek/trgov_naz   490x24 filter (mysql_tip rejoin [ tname "/trgov_naz" ])
      ]
    ]
    [
    	append tabla_blk compose
    	[
	    	at (p1 + 500x10) button "Promeniti" 100x24 color_m2
	      [
	        filter_tab: make block! []
	        foreach [ poz0 row0 ] filter_rek
	        [
	          append filter_tab copy poz0
	          append filter_tab construct/with [] row0
	        ]
	        filter_blk: filter_tab
	        parameter_ablak
	      ]
      ]
      either firma_rek/partner_baze_pod <> ""  
	    [
	      append tabla_blk compose
	      [
	        at (p1 + 110x190) info wrap rek/trans_naz 490x40 color_g2
	        at (p1 + 110x235) w_field rek/trgov_naz   490x24 filter (mysql_tip rejoin [ tname "/trgov_naz" ])
	      ]
	    ]
	    [
	      append tabla_blk compose
	      [
	        at (p1 + 110x190) w_field wrap rek/trans_naz 490x40 filter (mysql_tip rejoin [ tname "/trans_naz" ])
	        at (p1 + 110x235) w_field rek/trgov_naz      490x24 filter (mysql_tip rejoin [ tname "/trgov_naz" ])
	      ]
	    ]
  	]
    append tabla_blk compose
    [
      at (p1 + 110x40)  info wrap rek/naz_mat  490x40 color_g2
      at (p1 +   0x115) text right ~"Name:"    110x24
      at (p1 + 110x115) info wrap rek/name     490x40 color_g2
      at (p1 +   0x160) text right ~"Name hu:" 110x24
      at (p1 + 110x160) w_field rek/name_hu   490x24 filter (mysql_tip rejoin [ tname "/name_hu" ]) ;
      at (p1 +   0x190) text as-is middle right ~"Tranzit Naziv:"  110x24
      at (p1 +   0x220) text as-is middle right ~"Trgov. Naziv:"   110x24
    ]
    either find [ "prg" "adm" "sis" ] dozvola
    [
    	append tabla_blk compose
	    [
    		at (p1 + 110x85) w_field rek/naziv2  490x24 filter (mysql_tip rejoin [ tname "/naziv2" ])
  		]
    ]
    [
    	append tabla_blk compose
	    [
	      at (p1 + 110x85) info rek/naziv2     490x24 color_g2
	    ]
    ]
    either br_crt = 0 
    [  
      c1: color_b2
      t1: ~"Crtež -" 
    ]
    [ 
      c1: color_m2
      t1: ~"Crtež +"
    ]
    append tabla_blk compose
    [
      at (p1 + 10x270) button (t1) 80x24 (c1)
      [
        row1: make object! compose
        [
          sif: (copy rek/sif_mat)
          nev: (copy rek/naz_mat)
          nar: (copy "")
          tip: (#"M") ; Materijal
          arh: (write_mode)
          sel: ("V")  ; View
          dok: none
        ]
        mapa_kereso row1
      ]
      at (p1 + 100x270) button ~"Promet" 90x24 color_b2
      [
        mater_kartica mater_sif
        ; mat_lekeres
      ]
      at (p1 + 200x270) button ~"Nabavka" 90x24 color_b2
      [
        pregled_prom_nabavke join "M-" mater_sif
      ]
      at (p1 + 300x270) button ~"Nab.pod." 90x24 color_l2
      [
        lista_dobavljaca (join "M-" mater_sif)
      ]
      at (p1 + 400x270) button ~"Sastavnica" 100x24 color_b2
      [
        insert/only nyomtar reduce [ program_nev mater_sif ] 
        nyomelem: copy reduce [ rejoin [ "M-" mater_sif ] false ]
        program_nev: ~"Sastavnica" program_indito
      ]
      at (p1 + 510x270) button ~"Ugradnja" 90x24 color_b2 
      [
        ugradnja_sastavnice rejoin [ "M-" mater_sif ] "mat"
      ]
    ]
    p1: 0x300
    foreach sif1 g_baze_sif
    [
      row0: filter_keres sif1 
      if object? row0
      [
        if row0/nivo <> "b"
        [
          sz1: copy row0/subvalue
          if (row0/tip <> "a") and (row0/sifra <> "") [ sz1: get in rek to-word row0/sifra ]
          if sz1 <> ""
          [
            append tabla_blk compose
            [
              at (p1) text right (join (copy/part row0/naziv 12) ":") 110x24
              at (p1 + 110x0) info (sz1) 190x24 color_g2
            ]
            either p1/x = 0 [ p1/x: 300 ][ p1/x: 0 p1/y: p1/y + 30 ]
          ]
        ]
      ]
    ]
    if p1/x > 0 [ p1/x: 0 p1/y: p1/y + 30 ]
    either (firma_rek/kalkul_barkoda = "T") or (string? find firma_rek/barkod_tiplist "M")
    [
      append tabla_blk compose
      [
        at (p1) text right ~"Barkod:" 110x24
        at (p1 + 110x0) info center rek/bar_kod 130x24 color_g2
      ]
    ]
    [
      append tabla_blk compose
      [
        at (p1) text right ~"Barkod:" 110x24
        at (p1 + 110x0) field rek/bar_kod 130x24 
      ]
    ]
    append tabla_blk compose
    [
      at (p1 + 250x0) button "Etiketa" 90x24 color_l2
      [ 
        row: kreiranje_etikete "M"
        row/sif: copy rek/sif_mat
        row/naz: copy rek/naz_mat
        row/bkod: copy rek/bar_kod
        row/bkat: copy ""
        row/kat: copy ""
        row/part: copy "Distributer:"
        row/bgod: copy ""
        row/god: copy ""
        row/cena: copy rek/prod_cena
        row/brdok:  copy ""
        row/datdok: sistime/isodate
        stampanje_etikete reduce [ row ]
      ]
    ]
    ;
    p1: p1 + 0x30
    if rek/jed_mere = "" [ rek/jed_mere: copy "kg" ]
    either rek/faza_naz = "Z"
    [
       append tabla_blk compose/only
      [
        at (p1) text right ~"Jed. mere:" 110x24
        at (p1 + 110x0) info center rek/jed_mere 90x24 color_g2
      ]
    ]
    [
      append tabla_blk compose/only
      [
        at (p1) text right ~"Jed. mere:" 110x24
        at (p1 + 110x0) drop-down 90x24 data (jedmere_lista "0")
        with [ rows: 10 text: rek/jed_mere ]
        [
          if string? face/data
          [
            rek/jed_mere: copy face/text
          ]
        ]
      ]
    ]
    append tabla_blk compose/only
    [
      at (p1 + 300x0) text right ~"Predv.mag:" 110x24
      at (p1 + 410x0) arrow right 24x24 color_l2
      [
        tabla_keres "pregled_magacina"
        tabla/value: reduce [ novi_sif ]
        izbor_sifre none
        if retval
        [
          rek/predv_mag: copy retval/1
          mod_mater
        ]
      ]
      at (p1 + 440x0) info center rek/predv_mag 50x24 color_g2
      at (p1 + 480x0) text right ~"Tarifa:" 70x24
      at (p1 + 550x0) drop-down data deftarif/mlist with [ text: rek/tarif ] 50x24
      [
        if string? face/data
        [
          rek/tarif: copy face/text
          rek/tar_grupa: copy blk_find deftarif/mlist deftarif/mgrup rek/tarif
        ]
      ]
    ]
    p1: p1 + 0x30
    either rek/tip = "R"
    [
      either rek/faza_naz = "Z"
      [
        append tabla_blk compose/only
        [
          at (p1) text right ~"Jedin.tež.:" 110x24
          at (p1 + 110x0) info center rek/inf_koef 90x24 color_g2
          at (p1 + 230x0) text right "Lock:" 50x24
          at (p1 + 280x0) info center rek/jtez_lock 34x24 color_g2
          at (p1 + 200x0) text center "kg" 30x24
          at (p1 + 310x0) text right ~"po jed.:" 90x24
          at (p1 + 400x0) info center rek/inf_jed 70x24 color_g2
        ]
      ]
      [
        append tabla_blk compose/only
        [
          at (p1) text right ~"Jedin.tež.:" 110x24
          at (p1 + 110x0) w_field rek/inf_koef 90x24 filter (mysql_tip rejoin [ tname "/inf_koef" ])
          at (p1 + 230x0) text right "Lock:" 50x24
          at (p1 + 280x0) check 24x24 (rek/jtez_lock = "Z") 24x24
          [
            either face/data [ rek/jtez_lock: "Z" ][ rek/jtez_lock: "-" ]
          ]
          at (p1 + 200x0) text center "kg" 30x24
          at (p1 + 310x0) text right ~"po jed.:" 90x24
          at (p1 + 400x0) drop-down 70x24 data (jedmere_lista "0")
          with [ rows: 10 text: rek/inf_jed ]
          [
            if string? face/data
            [
              rek/inf_jed: copy face/text
            ]
          ]
        ]
      ]
    ]
    [
      append tabla_blk compose
      [
        at (p1) text right "Težina:" 110x24
        at (p1 + 110x0) w_field rek/tezina 90x24 filter (mysql_tip rejoin [ tname "/tezina" ])
        at (p1 + 200x0) text center "kg" 30x24
      ]
    ]
    ;
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1)  text right ~"Plan_cena:" 110x24
      at (p1 + 110x0) w_field rek/pl_cena 110x24 filter (mysql_tip rejoin [ tname "/pl_cena" ])
      at (p1 + 220x0) text center "RSD" 50x24
      at (p1 + 260x0) text right ~"Zad.nab.cena:" 120x24
      at (p1 + 380x0) info center (get in firma_rek 'metod_zad_cene) 20x24 color_g2
      at (p1 + 400x0) info right (dec_form rek/zad_cena 0 4) 110x24 color_g2
      at (p1 + 510x0) text center (rejoin ["(" rek/dat_cene ")"]) 110x24
    ]
    mod_cen: true
    mod_ozn: true
    if firma_rek/metod_prodcene = "T"
    [
      if not find kor_specijal "oznrobe" 
      [ 
        mod_ozn: false
        if (not none? find rek/oznaka "C") or (not none? find rek/oznaka "K") [ mod_cen: false ]
      ]
    ]
    ; ---- cena valtoztatas ----
    p1: p1 + 0x30
    either mod_cen
    [
      p1/x: 0
      append tabla_blk compose
      [
        at (p1) text right ~"Ino cena:" 110x24
        at (p1 + 110x0) w_field rek/e_cen 110x24 filter (mysql_tip rejoin [ tname "/e_cen" ])
        at (p1 + 225x0) drop-down data g_valute 65x24 with [ rows: 5 text: rek/valuta ]
        [
          if string? face/data [ rek/valuta: copy face/text ]
        ]
        at (p1 + 280x0) text right ~"VP-cena:" 120x24
        at (p1 + 400x0) info right (dec_form rek/vp_cena 0 4) 110x24 color_g2
        at (p1 + 510x0) text left (rejoin [ "(" rek/dat_vpc ", " rek/kor_vpc ")" ]) 200x24
        at (p1 + 520x30) button "Kalk-cene" 80x24 color_l2
        [
          nova_cena_materijala
        ]
      ]
    ]
    [
      p1/x: 0
      append tabla_blk compose
      [
        at (p1) text right ~"Ino-cena:" 100x24
        at (p1 + 110x0) info center (dec_form rek/e_cen 0 4) 110x24 color_g2
        at (p1 + 215x0) text center rek/valuta 50x24 
        at (p1 + 280x0) text right ~"VP-cena:" 120x24
        at (p1 + 400x0) info right (dec_form rek/vp_cena 0 4) 110x24 color_g2
        at (p1 + 510x0) text center (rejoin [ "(" rek/dat_vpc ")"]) 110x24
      ]
    ]
    p1/x: 0 p1/y: p1/y + 30
    append tabla_blk compose
    [
      at (p1 + 10x0) text right ~"Šif.kase:" 100x24
      at (p1 + 110x0) field rek/sif_grupe 110x24
      at (p1 + 280x0) text right ~"VP-cena + PDV:" 120x24
      at (p1 + 400x0) info right (dec_form rek/prod_cena 0 4) 110x24 color_g2
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1 + 280x0) text right ~"MP-cena:" 120x24
      at (p1 + 400x0) info right (dec_form mp_rek/zad_cena 0 2) 110x24 color_g2
      at (p1 + 515x0) text left (rejoin [ "(" mp_rek/dat_cene ", " mp_rek/kor_cene ")" ]) 200x24
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) text right "Kalk.nab.cena:" 110x24
      at (p1 + 110x0) button "Podaci kalk.cene" 150x24 color_l2 
      [
        if rek/pod_ncena = ""
        [
          alert "Polje za nab. cene je prazan u bazi! Prvo uradite obradu za kalk. nab. cene. "
          exit
        ]
        tar_1: (zadnje_cene_sql_select rek/sif_mat 1 1)
        append tar_1 (zadnje_cene_sql_select rek/sif_mat 16 1)
        zadnje_cene_mat_ablak tar_1
      ]
      at (p1 + 280x0) text right ~"MP-cena + PDV:" 120x24
      at (p1 + 400x0) info right (dec_form mp_rek/prod_cena 0 2) 110x24 color_g2
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1 + 110x0) 
      tlab center "Mag."          40x28 color_h1 color_h2 
      tlab center "Lok."          40x28 color_h1 color_h2
      tlab center "Cena (EUR)"   100x28 color_h1 color_h2
      tlab center "Cena (RSD)"   100x28 color_h1 color_h2
      tlab center "Cene osvežio" 150x28 color_h1 color_h2
    ]
    mysql_cmd
    [
      "SELECT t1.sif_mag,t1.lok_oour,0 AS d_cena,0 AS e_cena,'' AS mod_cena FROM " firma/data ".magacin AS t1 "
      "WHERE t1.vrsta='materijal' AND t1.tip_mag='M' AND t1.lok_oour<>'' ORDER BY t1.sif_mag ASC"
    ]
    mag_tar: get_rekordset/index copy db
    ;
    blk1: parse/all rek/pod_ncena "^/"
    poz1: head blk1
    while [ not tail? poz1 ]
    [
      poz1/1: parse/all poz1/1 "|"
      poz1: next poz1
    ]
    ;
    p1: p1 + 0x4
    foreach [ msif mrow ] mag_tar
    [
      mrow/d_cena: dec_value mrow/d_cena
      mrow/e_cena: dec_value mrow/e_cena
      ;
      poz1: head blk1
      while [ not tail? poz1 ]
      [
        if msif = poz1/1/1
        [
          mrow/d_cena:   round/to (dec_value poz1/1/2) 0.0001
          mrow/e_cena:   round/to (dec_value poz1/1/3) 0.0001
          mrow/mod_cena: rejoin [ poz1/1/4 ", " poz1/1/5 ]
        ]
        poz1: next poz1
      ]
      p1: p1 + 0x23
      append tabla_blk compose/only
      [
        at (p1 + 110x0)
        tlab center (mrow/sif_mag)   40x24 color_t1 color_t2
        tlab center (mrow/lok_oour)  40x24 color_t1 color_t2
        tlab right  (to-string mrow/e_cena) 100x24 color_b1 color_b2
        with (compose/only [ data: (mrow) ])
        [
          write clipboard:// (face/text)
        ]
        tlab right  (to-string mrow/d_cena) 100x24 color_b1 color_b2
        with (compose/only [ data: (mrow) ])
        [
          write clipboard:// (face/text)
        ]
        tlab right  (mrow/mod_cena) 150x24 color_t1 color_t2
      ]
      
    ]
    p1: p1 + 0x24
    append tabla_blk compose/only
    [
      at (p1 + 110x0) text "* kliknite cene: kopirati kao Ctrl+C"  400x24
    ]
    p1: p1 + 0x25
    ; ---- oznaka valtoztatas ----
    either mod_ozn
    [
      append tabla_blk compose
      [
        at (p1) text right "Ozn. cene:" 110x24 
        at (p1 + 110x0) drop-down data [ "" "C" "K" ] with [ rows: 3 text: rek/oznaka ] 50x24
        [
          if string? face/data [ rek/oznaka: copy face/text ]
        ]
      ]
    ]
    [
      append tabla_blk compose
      [
        at (p1) text right "Ozn. cene:" 100x24 
        at (p1 + 110x0) info center rek/oznaka 30x24 color_g2
      ]
    ]
    append tabla_blk compose
    [
      at (p1 + 280x0) text right "Namena:" 120x24 
      at (p1 + 400x0) drop-down data [ "" "MON" ] with [ rows: 2 text: rek/namena ] 70x24
      [
        if string? face/data [ rek/namena: copy face/text ]
      ]
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) text right "Tarifa carine:" 110x24
    ]
    blk0: parse/all rek/oblik_car "|"
    either (length? blk0) > 0
    [
    	sz0: rejoin [ "Otvori tar.car. (" (to-string (length? blk0)) ")" ]
    	append tabla_blk compose
	    [
	      at (p1 + 110x0) tlab sz0 190x24 color_t1 color_t2
	      [
	      	ablak_tarif_car
	      ]
	    ]
  	]
    [	
	    either block? find kor_specijal "tarcarmat"
	    [
	      append tabla_blk compose
		    [
		      at (p1 + 110x0) w_field rek/tarif_car 165x24 filter (mysql_tip rejoin [ tname "/tarif_car" ])
		      at (p1 + 275x0) button "+" 24x24 color_b2 [ ablak_tarif_car ]
		    ]
	    ]
	    [
		    append tabla_blk compose
		    [
		      at (p1 + 110x0) info rek/tarif_car 190x24 color_g2
		    ]
	    ]
    ]
    either rek/faza_naz = "Z"
    [
      append tabla_blk compose
      [
        at (p1 + 300x0) text right "Bris:" 100x24
        at (p1 + 400x0) info center rek/bris 70x24 color_g2
      ]
    ]
    [
      append tabla_blk compose
      [
        at (p1 + 300x0) text right "Bris:" 100x24
        at (p1 + 400x0) drop-down data bris_list 70x24 with [ rows: 4 text: rek/bris ]
        [
          if string? face/data [ rek/bris: copy face/text ]
        ]
      ]
    ]
    p1: p1 + 0x30
    append tabla_blk compose
    [
      at (p1) text right ~"Dimenzija:" 110x24 
      at (p1 + 110x0) field rek/dimenzija 190x24
      at (p1 + 280x0) text right "Boja:" 120x24
      at (p1 + 400x0) field rek/boja 200x24
    ]
    p1: p1 + 0x30
    either object? kat_blk/1 [ c1: color_m2 ][  c1: color_b2 ]
    append tabla_blk compose/only
    [
      at (p1) text right (rejoin [ firma_rek/kat_br_mat1 ":" ]) 110x21
      at (p1 + 110x0) field rek/katbr1 166x24
      at (p1 + 276x0) button "+" 24x24 (c1) with (compose [ user-data: 1 ])
      [
        kataloski_podaci rejoin [ "M-" rek/sif_mat ] face/user-data
        mod_mater
      ]
      at (p1 + 330x0) t_nev ~"Grupa:" 70x24 
      at (p1 + 400x0)  arrow right 24x24 color_l2
      [
        tabla_keres "mat_grupe_prikaz"
        tabla/value: reduce [ rek/mat_grupa ]
        tabla/filter: rejoin [ "grupa=" apjel "M" apjel ]
        izbor_sifre [ 'btn ~"Brisati" ]
        if retval
        [
          either retval = ~"Brisati" [ rek/mat_grupa: copy "" ][ rek/mat_grupa: copy retval/2 ]
          mod_mater
        ]
      ]
      at (p1 + 430x0) info center rek/mat_grupa 80x24 color_g2
    ]
    if rek/tip = "T"
    [
      p1: p1 + 0x30
      append tabla_blk compose
      [
        at (p1)  t_nev ~"Vrsta sirov.:" 100x24 
        at (p1 + 110x0)  arrow right 24x24 color_l2
        [
          tabla_keres "vrste_sirov_izb"
          izbor_sifre [ 'btn "Brisati" ]
          if retval = ~"Brisati" [ rek/vrsta_sir: copy "" ]
          if block? retval
          [
            rek/vrsta_sir: copy retval/1
            rek/naziv_sir: get_table_value "naziv" retval
          ]
          mod_mater 
        ]
        at (p1 + 140x0) info center rek/vrsta_sir  60x24 color_g2
        at (p1 + 205x0) text middle rek/naziv_sir 200x24
      ]
    ]
    p1: p1 + 0x50
    append tabla_blk compose
    [
    	at (p1 + 110x0) button ~"Modif" 90x24 color_m2 
      [
      	; suly szamitas kell
        ;
        if rek/tip = "R"
        [
          sz1: kalkulacija_geometrije rek none
          if string? sz1 [ alert sz1 exit ]
        ]
        hb: materijal_kiir 
        if not hb [ alert "Greška kod ispisa podataka!" exit ]
        ablak_cezar
      ]
      at (p1 + 210x0) button "Brisati" 90x24 color_m4
      [
        if firma_rek/materijal_orig = "C" [ alert "Nije dozvoljeno!" exit ]
        if not program_mode [ alert "Nije dozvoljeno!" exit ]
        kontrola_brisanje_materijala
        ;if not retval [ ablak_cezar exit ]
      ]
  	]
  	append tabla_blk compose
    [
    	at (p1 + 310x0) text right  "Zatv.naziv.mat:"  150x24
    	at (p1 + 460x0) info center (rek/faza_naz)      50x24 color_g4
  	]
  	if block? find kor_specijal "zatnazmat"
  	[  		
	    either rek/faza_naz = "Z"
	    [
		    append tabla_blk compose
		    [
		    	at (p1 + 505x0) button "Otvori"  95x24 color_m2 
		      [
		      	rek/faza_naz: copy ""
		        hb: materijal_kiir 
		        if not hb [ alert "Greška kod ispisa podataka!" exit ]
		        mod_mater
		        ;-- kihuzva, nem biztos hogy kell
		        ;logfh: make string! 1000
            ;transfer_podataka_partnera "M" rek/sif_mat
		      ]
		    ]
	    ]
	    [
		    append tabla_blk compose
		    [
		      at (p1 + 505x0) button "Zatvori" 95x24 color_b2
		      [
		      	rek/faza_naz: copy "Z"
		        hb: materijal_kiir 
		        if not hb [ alert "Greška kod ispisa podataka!" exit ]
		        mod_mater
		        ;-- kihuzva, nem biztos hogy kell
            ;logfh: make string! 1000
            ;transfer_podataka_partnera "M" rek/sif_mat
		      ]
		    ]
	    ]
    ]
    p1: p1 + 0x40
    append tabla_blk compose
    [
      at (p1 + 10x0) text right "Inserted:" 100x24
      at (p1 + 110x0) info rek/kor_insert 95x24 color_g2
      info rek/dat_insert 95x24 color_g2
      at (p1 + 295x0) text right "Modified:" 105x24
      at (p1 + 400x0) info rek/kor_modif 100x24 color_g2
      info rek/dat_modif  100x24 color_g2
    ]
    p1: p1 + 0x40
    poz0: head rek/naziv_list
    while [ not tail? poz0 ]
    [
      sz2: copy poz0/2
      replace/all sz2 "#n" "^\"
      y1: max 27 (2 + second text-pixel-size/wsize poz0/2 490x700)
      append tabla_blk compose
      [
        at (p1 + 10x0) tlab (poz0/1) (as-pair 101 y1) color_t1 color_t2
        at (p1 + 110x0) tlab as-is (sz2) (as-pair 490 y1) color_t1 color_t2
      ]
      p1: p1 + (as-pair 0 (y1 - 1))
      poz0: next next poz0
    ]
    p1: max (p1 + 0x60) ablak_c/size
    ablak_c/pane: layout/offset tabla_blk ablak_c/user-data
    bepara/scroll: 0x0
    show ablak_c
  ]
  ;
  nova_cena_materijala: func [/local t_vpcena cena pr1 ]
  [
    tabla_blk: compose
    [
      size 500x140
      backtile polished color_g4
      across
      at 9x9 button close_btn_img 26x26
      [
        hide-popup
      ]
      at 10x40 text right "Veleprodajna cena:" 190x24
      at 200x40 t_vpcena: field 140x24
      at 370x40 button "Upis cene" 100x24 color_m2
      [
        pr1: (100 + dec_value rek/tarif) / 100
        cena: (dec_value t_vpcena/text) * pr1
        rek/prod_cena: round/to cena 0.01
        cena: dec_value rek/prod_cena
        rek/vp_cena: round/to (cena / pr1) 0.000001
        rek/dat_vpc: sistime/isodate
        rek/kor_vpc: korisnik
        mysql_cmd
        [
          "UPDATE " tname " SET vp_cena='" rek/vp_cena "',prod_cena='" rek/prod_cena "',"
          "dat_vpc='" rek/dat_vpc "',kor_vpc='" rek/kor_vpc "' "
          "WHERE sif_mat='" rek/sif_mat "'"
        ]
        mod_mater
        bepara/scroll: 0x0
        hide-popup
      ]
      at 10x90 text right "Maloprodajna cena:" 190x24
      at 200x90 center rek/prod_cena 140x24 color_g2
    ]
    ablak_p: layout/offset tabla_blk 0x0
    t_vpcena/text: copy (to-string rek/vp_cena) show t_vpcena
    ;
    inform/title ablak_p "Nova cena materijala"
  ]
  ; filter_rek kreal a rekord adatai alapjan
  filter_kreal: func [/local poz0 row0 row1 sif1 val1 rbr1 mbr1 ]
  [
    filter_rek: clear_filter
    poz0: head filter_blk
    while [ not tail? poz0 ]
    [
      ; kategoria:
      row0: poz0/2
      row0/value: copy ""
      if row0/sifra = "" [ break ]
      sif1: to-word row0/sifra
      if not string? val1: get in rek sif1 [ break ]
      row0/value: copy val1
      foreach [ rbr1 mbr1 ] row0/sastav
      [
        row1: select klas_tar mbr1
        if object? row1
        [
          if row1/sifra = row0/value
          [
            sif1: join row0/poz "."
            filter_bovit row1 sif1
            break
          ]
        ]
      ]
      poz0: next next poz0
    ]
    sort/skip filter_blk 2
  ]
  ;
  filter_keres: func [ sif1[string!] /local poz0 row0 row1 rbr1 mbr1 ]
  [
    poz0: head filter_blk
    while [ not tail? poz0 ]
    [
      ; kategoria:
      row0: poz0/2
      if row0/sifra = sif1
      [
        ; subvalue feltoltest a filter-ablakban is lehetne
        foreach [ rbr1 mbr1 ] row0/sastav
        [
          row1: select klas_tar mbr1
          if object? row1
          [
            if row1/sifra = row0/value
            [
              row0/subvalue: copy row1/naziv
            ]
          ]
        ]
        return row0
      ]
      poz0: next next poz0
    ]
    return none
  ]
  
  filter_torol: func [ sif1[string!] /local poz1 n1 ]
  [
    n1: length? sif1
    poz1: head filter_blk
    while [ not tail? poz1 ]
    [
      either (copy/part poz1/1 n1) = sif1 
      [ 
        row1: poz1/2
        row1/value: copy ""
        remove/part poz1 2 
      ]
      [ poz1: next next poz1 ]
    ]
  ]
  
  filter_bovit: func [ row1[object!] sif1[string!] /local row2 rbr2 mbr2 i1 n1 row2a ]
  [
    i1: length? row1/sastav
    n1: 0
    while [ i1 > 0 ]
    [
      i1: to-integer (i1 / 10)
      n1: n1 + 1
    ]
    i1: 0
    if n1 < 1 [ n1: 1 ]
    foreach [ rbr2 mbr2 ] row1/sastav
    [
      row2: select klas_tar mbr2
      if object? row2
      [
        i1: i1 + 1
        row2a: construct/with [] row2
        row2a/poz: join sif1 int_form/zerofill i1 n1
        row2a/value: copy ""
        append filter_blk row2a/poz
        append filter_blk row2a 
      ]
    ]
  ]
  
  clear_filter: func []
  [
    jed_mere: copy ""
    filter_blk: make block! []
    ; beletenni az alap-rekordot:
    row0: select klas_tar klas_mbr
    if object? row0 
    [ 
      filter_bovit row0 ""
    ]
    return filter_blk
  ]
  
  klasifikacija_olvas: func [/local ind0 row0 row1 sif1 ]
  [
    ; teljes klasifikacios tar olvasas:
    mysql_cmd 
    [
      "SELECT mbr,klasa,rbr,sifra,ozn,tip,nivo,obav,naziv,naz_list,frm_list,usl_list,pdci_list,jed_mere,poc_sifre,"
      "kor_modif,dat_modif,'' AS value,'' AS subvalue,'' AS poz,0 AS lista,0 AS sastav FROM klasif_robe "
      "WHERE oblast='" klas_oblast "' ORDER BY mbr ASC"
    ]
    klas_tar: get_rekordset/intkey copy db 
    klas_mbr: none
    hbszov: reduce [ make string! 10000 ]
    foreach [ ind0 row0 ] klas_tar
    [
      row0/mbr:   to-integer row0/mbr
      row0/rbr:   to-integer row0/rbr
      row0/klasa: to-integer row0/klasa
      row0/ozn:   to-integer row0/ozn
      row0/naz_list: parse/all row0/naz_list "|"
      row0/frm_list: parse/all row0/frm_list "|"
      row0/usl_list: parse/all row0/usl_list "|"
      row0/pdci_list: parse/all row0/pdci_list "|"
      if (row0/klasa = 0) and (row0/sifra = klas_oblast) [ klas_mbr: row0/mbr ]
      row0/sastav: make block! []
      if row0/klasa > 0
      [
        row1: select klas_tar row0/klasa
        if object? row1
        [
          ; hozza be az erteket
          if row1/tip <> "a" [ row1/value: copy row0/naziv ]
          append row1/sastav row0/rbr
          append row1/sastav row0/mbr
        ]
      ]
      if row0/sifra = ""
      [
        sif1: ""
        if object? row1 [ sif1: rejoin [ row1/sifra ":" row1/naziv ] ]
        append hbszov/1 rejoin [ "Fali šifra kod: " sif1 "/:" row0/naziv "^/" ]
      ]
      if row0/ozn = 1
      [
        if not find g_baze_sif row0/sifra
        [
          sif1: ""
          if object? row1 [ sif1: rejoin [ row1/sifra ":" row1/naziv ] ]
          append hbszov/1 rejoin [ "Pogrešno polje iz baze: " sif1 "/" row0/sifra ":" row0/naziv "^/" ]
        ]
      ]
    ]
    foreach [ ind0 row0 ] klas_tar
    [
      sort/skip row0/sastav 2
    ]
    if not empty? hbszov/1
    [
      nyomtatas_txt hbszov "klasif_robe.txt"      
      program_mode: false
    ]
  ]
  
  ; bal oldali tablazat:
  mat_tablazat: func [ /local sz1 sz2 sz3 sz4 poz0 row0 min_max ]
  [
    ; a kivalasztott raktar
    tabla_keres "materijal_klas1"
    tabla/akcio: func [ adat ]
    [
      mater_sif: copy adat/1
      mysql_cmd
      [
        "SELECT t1.*,IFNULL((SELECT t2.naziv FROM vrste_sirov AS t2 WHERE t2.vrsta=t1.vrsta_sir),'') AS naziv_sir "
        "FROM " tname " AS t1 WHERE t1.sif_mat='" mater_sif "' "
      ]
      rek: get_rekord first db
      either retval
      [
        rek/naziv_list: parse/all rek/naziv_list "|"
        filter_kreal
        mod_mater
      ]
      [
        alert ~"Materijal nije u bazi!" 
        mater_sif: copy "" 
      ]
      mat_tablazat 
    ]
    sz1: copy ""
    sz4: copy ""
    foreach [ poz0 row0 ] filter_tab
    [
      if (row0/sifra > "") and (row0/value > "-")
      [
        if sz1 <> "" [ append sz1 " AND " ]
        either (row0/tip = "a") or (row0/tip = "n")
        [
          either find [ "dim_a" "dim_b" "dim_c" ] row0/sifra
          [
            min_max: parse/all row0/value "-"
            either ((length? min_max) > 1) and (not none? find row0/value #"-")
            [
              append sz1 rejoin 
              [ 
                "(ROUND(" row0/sifra ",2)>=" apjel min_max/1 apjel 
                " AND ROUND(" row0/sifra ",2)<=" apjel min_max/2 apjel ")"
              ]
            ]
            [
              append sz1 rejoin [ "ROUND(" row0/sifra ",2)=" apjel min_max/1 apjel ]
            ]
          ]
          [
            append sz1 rejoin [ row0/sifra "=" apjel row0/value apjel ]
          ]
        ]
        [
          append sz1 rejoin [ row0/sifra " like " apjel "%" row0/value "%" apjel ]
        ]
      ]
    ]
    ;
    ; bris dropdown szures
    if keres_rek/bris = "A+B"
    [
      if sz4 <> "" [ append sz4 " AND " ]
      append sz4 rejoin
      [
        "((bris = " apjel "A" apjel ") OR (bris = " apjel "B" apjel " AND stanje_mag>0))"
      ]
    ]
    if keres_rek/bris = "B"
    [
      if sz4 <> "" [ append sz4 " AND " ]
      append sz4 rejoin [ "((bris = " apjel "B" apjel ") AND (stanje_mag=0))" ]
    ]
    if keres_rek/bris = "I"
    [
      if sz4 <> "" [ append sz4 " AND " ]
      append sz4 rejoin [ "(bris = " apjel "I" apjel ")" ]
    ]
    ;
    if keres_rek/nazdeo <> ""
    [
      if sz1 <> "" [ append sz1 " AND " ]
      ; a beirt szavak mindegyiket kulon keresse 4 mezoben
      sz2: parse/all keres_rek/nazdeo "%"
      sz3: copy ""
      foreach row0 sz2
      [
        if sz3 <> "" [ append sz3 " AND " ]
        append sz3 rejoin 
        [ 
          "(naziv_list LIKE " apjel "%" row0 "%" apjel " OR "
          "naz_mat LIKE " apjel "%" row0 "%" apjel " OR "
          "naziv2 LIKE " apjel "%" row0 "%" apjel " OR "
          "trans_naz LIKE " apjel "%" row0 "%" apjel " OR " 
          "name LIKE " apjel "%" row0 "%" apjel ") "
        ]
      ]
      append sz1 rejoin [ "(" sz3 ") " ]
    ]
    if keres_rek/standard <> ""
    [
      if sz1 <> "" [ append sz1 " AND " ]
      append sz1 rejoin [ "(stand_a LIKE " apjel "%" keres_rek/standard "%" apjel " OR "
                          "stand_b LIKE " apjel "%" keres_rek/standard "%" apjel ")" ]
    ]
    if keres_rek/katbr1 <> ""
    [
      if sz1 <> "" [ append sz1 " AND " ]
      append sz1 rejoin 
      [
        "(katbr1 LIKE " apjel "%" keres_rek/katbr1 "%" apjel " OR "
        "katbr2 LIKE " apjel "%" keres_rek/katbr1 "%" apjel " OR "
        "katbr3 LIKE " apjel "%" keres_rek/katbr1 "%" apjel ") "
      ]
    ]
    if keres_rek/proizv <> ""
    [
      if sz1 <> "" [ append sz1 " AND " ]
      append sz1 rejoin [ "proizv_distr LIKE " apjel "%" keres_rek/proizv "%" apjel ]
    ]
    tabla/filter: sz1
    tabla/filter2: sz4
    tabla/value:  none
    if mater_sif <> "" [ tabla/value: reduce [ mater_sif ] ]
    tabla/ablak:  ablak_b
    tabla_view
  ]
  ;
  set 'barkod_event: func [/local barkod ]
  [
    barkod: barkod_olvas
    if (length? barkod) <> 13 [ alert "Bar-kod nije dobar!" ablak_cezar exit ]
    if barkod/1 <> #"1" [ alert "Bar-kod nije za materijale!" ablak_cezar exit ]
    mysql_cmd
    [
      "SELECT t1.*,IFNULL((SELECT t2.naziv FROM vrste_sirov AS t2 WHERE t2.vrsta=t1.vrsta_sir),'') AS naziv_sir "
      "FROM materijal AS t1 WHERE t1.bar_kod='" barkod "' "
    ]
    rek: get_rekord first db
    if not retval [ alert rejoin [ "Bar-kod: " barkod " nije u bazi!" ] ablak_cezar exit ]
    mater_sif: copy rek/sif_mat
    mod_mater
  ]
  ;
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
      text nazad_nev 200x24 with [ keycode: 'f5 ]
	    [
      	 program_nev program_indito
	    ]
      at 300x10 text right "Bar-kod èitaè u funkciji" 200x24
      check 24x24 (port? ser_port)
      [
        either port? ser_port [ szerial_kapu/csuk ][ szerial_kapu/nyit ]
        face/data: (port? ser_port) show face
      ]
      at 550x10 button "Izveštaji" 100x24 color_l2
      [
        if mater_sif <> "" [ mater_sif: copy "" ]
        ablak_izvestaji
      ]
      at 840x10 button "Clear filter" 100x24 color_l2
      [
        filter_tab: clear_filter
        clear_keres_rek
        plan_naziv: copy ""
        ablak_cezar
      ]
      at 0x40 ablak_b: box color_g4 (as-pair meretb (merety - 60)) with [  edge: [color: coal size: 1x1 ] ]
      at (as-pair 0 (merety - 20)) slider (as-pair meretb 20) with [ user-data: ablak_b ]
      [ scroller_pozicio face ]
      at (as-pair meretb 40) slider (as-pair 20 (merety - 40)) with [ user-data: ablak_b ]
      [ scroller_pozicio face ]
      at (as-pair (meretb + 20) 40) ablak_c: box color_g4 (as-pair (meretx - meretb - 40) (merety - 40)) with [  edge: [color: coal size: 1x1 ] ]
      at (as-pair (meretx - 20) 40) slider (as-pair 20 (merety - 40)) with [ user-data: ablak_c ]
      [ scroller_pozicio face ]
    ] 0x0
    ; meretek:
    ablak_b/user-data: 0x0
    ablak_c/user-data: 0x0
    main_window/user-data: ablak_c ; gorgo bekapcsolasa
    show ablak_a
  ]
  
  set 'materijal_nov: func [ /local row0 ]
  [
    jm_lista: make block! [ "" "kom" "par" "gar" "set" "kg" "lit" "m" "m2" "m3" "mil" ]
    tname: copy "materijal"
    tabla_keres "materijal_klas1"
    ;
    bris_list: copy/deep [ "A" "B" ]
    if find [ "prg" "adm" ] dozvola [ append bris_list [ "D" "I" "P" ] ]
    jed_mere:  copy ""
    ;
    ;set_table_form [ uslov "t1.bris" "#:I" ]
    clear_keres_rek
    ;
    if not find kor_specijal "mat" [ program_mode: false ]
    klas_oblast: copy "mat"
    g_baze_sif: parse/all rejoin
    [
       ",faza_naz,tip,oblik,grupa_mat,podgr_mat,spec_karakt,spec_k_dod,term_obr,pov_stanje,"
       "sir_mat,proizv_distr,boja,stand_a,stand_b,dim_a,dim_b,dim_c,dim_d,dim_e,dim_f,"
       "dom_naz,ino_naz,katbr1,katbr2,katbr3,katbr4"
    ] ","
    ;
    klasifikacija_olvas
    ;
    ablak_arajz
    filter_tab: clear_filter
    ablak_cezar
    if not empty? nyomelem 
    [
      tabla/akcio nyomelem
      clear nyomelem
    ]
  ]
]

append cleanup
[
  materijal_nov_01
  materijal_nov
  osvez_car_tarif
]
