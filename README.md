# glicoCare
Si vuole progettare un sistema di telemedicina di un servizio clinico per la gestione di pazienti diabetici (diabete di tipo 2).

Il sistema deve permettere l’interazione di due attori principali, il diabetologo e il paziente.

Il paziente, dopo essersi autenticato, potrà memorizzare le rilevazioni giornaliere di glicemia (prima e dopo ogni pasto).

I livelli normali prima dei pasti dovrebbero essere compresi tra 80 e 130 mg/dL, mentre due ore dopo i pasti non dovrebbero superare i 180 mg/dL.

Il paziente può, inoltre, aggiungere eventuali sintomi (spossatezza, nausea, mal di testa, e così via), e le assunzioni di insulina e/o di farmaci antidiabetici orali, come da prescrizione dello specialista (giorno, ora, farmaco e quantità assunta). 

Eventuali sintomi, patologie e/o terapie concomitanti vanno opportunamente segnalati da parte del paziente, con l’indicazione del sintomo, della terapia e/o della patologia e del periodo associato.

Il diabetologo, dopo essersi autenticato, deve poter specificare le terapie che i pazienti devono seguire. 

Per ogni terapia il medico specifica il farmaco, il numero di assunzioni giornaliere, la quantità di farmaco per ogni assunzione, ed eventuali indicazioni (ad es., dopo i pasti, lontano dai pasti, e così via). 

Il medico potrà vedere i dati dei pazienti, anche in forma sintetica (ad es., andamento della glicemia settimana per settimana o mese per mese). 

Il medico potrà aggiungere o modificare la terapia a seconda dell’evoluzione dello stato del paziente. 

Il medico può, inoltre, aggiornare una breve sezione di informazioni sul paziente, contenente fattori di rischio (fumatore, ex-fumatore, problemi di dipendenza da alcol, o da stupefacenti, obesità), pregresse patologie, comorbidità presenti quali ipertensione, e così via.

Il sistema deve verificare che le assunzioni di farmaci da parte dei pazienti siano coerenti con le terapie prescritte. 

Il sistema deve invitare il paziente a completare gli inserimenti relativi alle assunzioni di farmaci, in modo da poter gestire sia alert verso il paziente, nel caso si dimenticasse di assumere i farmaci, sia verso il medico, nel caso il paziente non segua per più di 3 giorni consecutivi
le prescrizioni. 

Il sistema, inoltre, segnala ai medici tutti i pazienti che registrano glicemie oltre le soglie indicate con diverse modalità a seconda della gravità.

I responsabili del servizio inseriscono i dati iniziali di pazienti e medici, necessari per l’autenticazione.

Per ogni paziente è specificato un medico di riferimento, al quale il paziente può inviare email per richieste e domande varie. Ogni medico può vedere e aggiornare i dati di ogni paziente. Il sistema provvederà a tenere traccia di quale medico ha effettuato le varie operazioni.