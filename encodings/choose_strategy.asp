% ===== FATTI BASE E IDENTIFICAZIONE SISTEMI =====
% Identifica i sistemi controllati dall'IA
my_system(S) :- system(S), owner(S, P), ai_player(P).

% Identifica i giocatori nemici
enemy(P) :- system(S), owner(S, P), not ai_player(P).

% Identifica i sistemi nemici
enemy_system(S, P) :- system(S), owner(S, P), not ai_player(P).

% Connessioni non orientate tra sistemi
undirected_connected(M, S) :- connected(M, S).
undirected_connected(M, S) :- connected(S, M).

% ===== ANALISI TATTICHE =====
% Identifica i sistemi di confine
border_system(S) :- my_system(S), undirected_connected(S, N), not my_system(N).

% Sistemi di confine specifici per nemici e neutrali
border_to_enemy(S, N) :- my_system(S), undirected_connected(S, N), enemy_system(N, P).
border_to_neutral(S) :- my_system(S), undirected_connected(S, N), neutral_system(N).

% Identifica i sistemi ad alta produzione
high_production_system(S) :- production(S, P), P > 2.

% ===== CONTEGGI E METRICHE =====
% Conta i sistemi controllati dall'IA
my_system_count(C) :- #count{S : my_system(S)} = C.

% Conta i sistemi nemici per ogni giocatore nemico
enemy_system_count(P, C) :- enemy(P), #count{S : enemy_system(S, P)} = C.

% Somma delle navi nei sistemi controllati dall'IA
my_ships_total(T) :- #sum{Ships, S : my_system(S), ships(S, Ships)} = T.

% Somma delle navi nei sistemi nemici per ogni giocatore nemico
enemy_ships_total(P, T) :- enemy(P), #sum{Ships, S : enemy_system(S, P), ships(S, Ships)} = T.

% Calcola il totale delle navi in volo verso ogni sistema nemico
incoming_ships(SystemId, TotalIncoming) :-
    enemy_system(SystemId, Enemy),
    #sum{FleetShips : fleet(_, Enemy, FleetShips, _, SystemId)} = TotalIncoming.

% Regola principale: somma navi del sistema + navi in arrivo
enemy_system_ship(SystemId, TotalShips) :-
    enemy_system(SystemId, Enemy),
    ships(SystemId, Ships),
    incoming_ships(SystemId, IncomingShips),
    TotalShips = Ships + IncomingShips.

% Somma delle produzioni di tutti i sistemi del nemico per ogni nemico
enemy_production_total(P, T) :- enemy(P), #sum{Production, S : enemy_system(S, P), production(S, Production)} = T.


% Determina se un nemico si è rafforzato
enemy_strengthened(P) :-
    enemy(P),
    enemy_ships_total(P, Current),
    enemy_production_total(P, ProductionTotal),
    previous_enemy_ships_total(P, Previous),
    TotalPrevious = Previous + ProductionTotal * 2,
    Current > Previous.


border_enemy_system_strongest(My,Enemy) :-
    border_to_enemy(My, Enemy),
    enemy_system_ship(Enemy,EnemyShips),
    ships(My,MyShips),
    MyShips < EnemyShips.


% Verifica perdite significative (2 o più sistemi)
significant_losses :- #count{S : system_lost(S)} >= 2.


% Verifica se l'IA ha perso sistemi
systems_lost :- system_lost(_).

% Verifica se il numero di navi dell'IA è aumentato (solo se esistono dati precedenti)
ships_increased :-
    my_ships_total(Current),
    previous_my_ships_total(Previous),
    Current > Previous.

% Verifica se il numero di navi dell'IA è diminuito (solo se esistono dati precedenti)
ships_decreased :-
    my_ships_total(Current),
    previous_my_ships_total(Previous),
    Current < Previous.


% Condizioni per attacco cooperativo
cooperative_target(Enemy_S, P) :-
    enemy_system(Enemy_S, P),
    #count{My_S : border_to_enemy(My_S, Enemy_S)} =C,
    C>=2.

expansion_conditions :- border_to_neutral(S).

% 1) definisco chi ha almeno un confine con un nemico
has_enemy_border(S) :-
    border_to_enemy(S, _).

% 2) scelgo i sistemi miei che NON hanno alcun confine con nemici
candidate_my_system_helper(S) :-
    my_system(S),
    not has_enemy_border(S).

% Condizioni per strategia difensiva
defensive_conditions :- systems_lost.

% Condizioni per strategia difensiva
reinforce_conditions :-  border_enemy_system_strongest(_,_).

direct_attack_conditions:- border_to_enemy(S,P).

cooperative_attack_conditions(P) :- cooperative_target(_, P).

% Strategia di espansione applicabile
applicable_strategy(expansion) :- expansion_conditions.

% Strategia difensiva applicabile
applicable_strategy(defensive) :- defensive_conditions.

% Strategia difensiva applicabile
applicable_strategy(reinforce) :- reinforce_conditions.

% Strategia di attacco diretto applicabile
applicable_strategy(direct_attack) :- direct_attack_conditions.

% Strategia di attacco cooperativo applicabile
applicable_strategy(cooperative_attack) :- cooperative_attack_conditions(_).

% Per tutti i livelli di difficoltà, seleziona esattamente una strategia
{ chosen_strategy(S) : applicable_strategy(S) } <= N :- difficulty(D), num_strategy(N,D).

num_strategy(1,medium).
num_strategy(2,hard).

% ESPANSIONE: attacca sistemi neutrali vicini
target_neutral(From, To) :-
    chosen_strategy(expansion),
    my_system(From),
    neutral_system(To),
    undirected_connected(From, To),
    ships(From, Ships),
    Ships > 0.


% RINFORZO: rinforza sistemi di confine
reinforce_ships(From, To, Ships) :-
    chosen_strategy(reinforce),
    border_enemy_system_strongest(To, Enemy),
    enemy_system_ship(Enemy,EnemyShips),
    candidate_my_system_helper(From),
    undirected_connected(From, To),
    ships(From, FromShips),
    ships(To, ToShips),
    FromShips > 0,
    Ships = FromShips / 2,
    Ships > EnemyShips,
    Ships > 0,
    Ships < FromShips.

% DIFENSIVA : difende i confini di sistemi persi
defensive_ships(From, To, Ships) :-
    chosen_strategy(defensive),
    system_lost(LostSystem),
    ships(LostSystem, LostSystemShips),
    candidate_my_system_helper(From),
    my_system(To),
    undirected_connected(From, To),
    undirected_connected(To, LostSystem),
    ships(From, FromShips),
    ships(To, ToShips),
    FromShips > ToShips + 30,
    FromShips > 0,
    Ships = FromShips / 2,
    ToShips < LostSystemShips + 30,
    Ships > 0,
    Ships < FromShips.

expansion_ships(From, To, Ships) :-
    target_neutral(From, To),
    ships(From, TotalShips),
    Ships = (TotalShips * 3) / 4,
    Ships > 0,
    Ships < TotalShips.


% Calcola il totale delle navi in volo verso ogni sistema nemico
flying_ships(To, From,TotalIncoming) :-
    enemy_system(To, _),
    my_system(From),
    #sum{FleetShips : fleet(_, _, FleetShips, To, From)} = TotalIncoming.

% ATTACCO AGGRESSIVO (priorità alta) - quando ho molte più navi
aggressive_attack_possible(From, To) :-
    chosen_strategy(direct_attack),
    border_to_enemy(From, To),
    ships(From, FromShips),
    enemy_system_ship(To, ToShips),
    flying_ships(To,From,TotalIncoming),
    FromShips > (ToShips * 2) + TotalIncoming .



% ATTACCO AGGRESSIVO - mando una percentuale alta delle mie navi
direct_attack(From, To, AttackShips) :-
    aggressive_attack_possible(From, To),
    ships(From, FromShips),
    flying_ships(To,From, TotalIncoming),
    enemy_system_ship(To, ToShips),
    FromShips > AttackShips,
    AttackShips = ((FromShips * 3) / 4) + TotalIncoming.  % Mando il 75% delle mie navi

% ATTACCO RILASSATO - solo se non posso fare l'aggressivo
direct_attack(From, To, AttackShips) :-
    chosen_strategy(direct_attack),
    border_to_enemy(From, To),
    ships(From, FromShips),
    enemy_system_ship(To, ToShips),
    flying_ships(To,From,TotalIncoming),
    production(To, Pr),
    FromShips > AttackShips,
    not aggressive_attack_possible(From, To),  % NON posso fare attacco aggressivo
    AttackShips = (ToShips + TotalIncoming) + (30 * Pr).


% Identifica tutti i sistemi che possono partecipare all'attacco coordinato
coordinated_attackers(From, To) :-
    chosen_strategy(cooperative_attack),
    border_to_enemy(From, To),
    ships(From, FromShips),
    enemy_system_ship(To, ToShips),
    FromShips > ToShips / 3.

% Conta quanti sistemi possono attaccare ciascun bersaglio
attackers_count(To, Count) :-
    enemy_system(To, _),
    chosen_strategy(cooperative_attack),
    #count{From : coordinated_attackers(From, To)} = Count.

% Seleziona il bersaglio con il maggior numero di potenziali attaccanti
best_coordinated_target(To) :-
    chosen_strategy(cooperative_attack),
    attackers_count(To, Count),
    Count >= 2,
    #max{C, T : attackers_count(T, C)} = Count.

% ===== COOPERATIVE ATTACK AGGRESSIVO (priorità alta) =====
% Controlla se è possibile un attacco cooperativo aggressivo
aggressive_cooperative_possible(To) :-
    chosen_strategy(cooperative_attack),
    best_coordinated_target(To),
    enemy_system_ship(To, ToShips),
    flying_ships(To, _, TotalIncoming),
    attackers_count(To, AttackerCount),
    #sum{FromShips : coordinated_attackers(From, To), ships(From, FromShips)} = TotalAttackerShips,
    TotalAttackerShips >  ((ToShips + TotalIncoming) * 5)/2 .  % Forza schiacciante disponibile

% ATTACCO COOPERATIVO AGGRESSIVO - devastazione totale
cooperative_ships(From, To, Ships) :-
    aggressive_cooperative_possible(To),
    coordinated_attackers(From, To),
    ships(From, TotalShips),
    flying_ships(To, From, MyIncoming),
    Ships = ((TotalShips * 3) / 4) + MyIncoming,  % 75% delle mie navi + compenso per le mie navi in volo
    Ships <= TotalShips.

% ===== COOPERATIVE ATTACK RILASSATO (fallback) =====
% ATTACCO COOPERATIVO RILASSATO - solo se non posso fare l'aggressivo
cooperative_ships(From, To, Ships) :-
    chosen_strategy(cooperative_attack),
    best_coordinated_target(To),
    coordinated_attackers(From, To),
    not aggressive_cooperative_possible(To),  % NON posso fare attacco aggressivo
    ships(From, TotalShips),
    enemy_system_ship(To, ToShips),
    production(To, ToProd),
    attackers_count(To, AttackerCount),
    flying_ships(To, From, MyIncoming),
    RequiredTotal = ToShips + (ToProd * 50),  % Buffer produzione ridotto per cooperativo
    MyShare = RequiredTotal / AttackerCount,
    Ships = MyShare + MyIncoming,
    Ships <= (TotalShips * 2) / 3.  % Non più del 66% delle mie navi

% ===== AZIONI FINALI CORRETTE =====
{send_expansion_fleet(From, To, Ships): expansion_ships(From, To, Ships)} = 1 :-
    chosen_strategy(expansion).

{send_defensive_fleet(From, To, Ships): defensive_ships(From, To, Ships)} = 1 :-
    chosen_strategy(defensive).

{send_reinforce_fleet(From, To, Ships): reinforce_ships(From, To, Ships)} = 1 :-
    chosen_strategy(reinforce).

{send_attack_fleet(From, To, Ships): direct_attack(From, To, Ships)} = 1 :-
    chosen_strategy(direct_attack).

% Per attacco cooperativo, genera azioni solo per il bersaglio selezionato
{send_cooperative_fleet(From, To, Ships): cooperative_ships(From, To, Ships)} >= 2 :-
    chosen_strategy(cooperative_attack),
    best_coordinated_target(To).

% Evita conflitti tra attacco diretto e cooperativo
:- send_attack_fleet(F1, T, S1), send_cooperative_fleet(F2, T, S2).

:- send_fleet(From,To1,_), send_fleet(From,To2,_), To1!=To2.

% Assicurati che tutti gli attacchi cooperativi vadano allo stesso bersaglio
:- send_cooperative_fleet(F1, T1, S1), send_cooperative_fleet(F2, T2, S2), T1 != T2.

% Assicurati che ci siano almeno 2 sistemi nell'attacco cooperativo
:- chosen_strategy(cooperative_attack), #count{From : send_cooperative_fleet(From, _, _)} < 2.

send_fleet(From,To,Ships) :- send_expansion_fleet(From,To,Ships).
send_fleet(From,To,Ships) :- send_defensive_fleet(From,To,Ships).
send_fleet(From,To,Ships) :- send_attack_fleet(From,To,Ships).
send_fleet(From,To,Ships) :- send_cooperative_fleet(From,To,Ships).
send_fleet(From,To,Ships) :- send_reinforce_fleet(From,To,Ships).

% Evita azioni che lascerebbero sistemi vulnerabili
% :- send_fleet(From, _, Ships), ships(From, Total), Ships > Total.


% Assicurati che le navi inviate siano positive
:- send_fleet(_, _, Ships), Ships <= 0.


% Vieni penalizzato se scegli meno strategie di quanto ne sono applicabili
:~ num_strategy(N,D), difficulty(D), #count{ S : chosen_strategy(S)} = C, Z=N-C. [Z@7]

:~ applicable_strategy(expansion), not chosen_strategy(expansion). [1@6]

% Vieni penalizzato se puoi attaccare due nemici diversi ma attacchi quello con piu sistemi
:~ direct_attack(F1,To1,_),
    direct_attack(F2,To2,_),
    send_fleet(F1,To1,_),
    enemy_system(To1,P1),
    enemy_system(To2,P2),
    enemy_system_count(P1,C1),
    enemy_system_count(P2,C2),
    P1!=P2,
    C1>C2,
    N=C1-C2. [N@5]

% Vieni penalizzato se hai perso sistemi e non ti difendi
:~ significant_losses, not chosen_strategy(defensive). [15@4]
% Vieni penalizzato se hai come strategia applicabile la cooperative ma non la scegli
:~ applicable_strategy(cooperative_attack), chosen_strategy(S), S != cooperative_attack. [10@4]

% Vieni penalizzato se ti espandi verso un sistema che ha un tasso di produzione piu basso rispetto ad un altro
:~ send_expansion_fleet(_,To,_), production(To,P), Z=5-P. [Z@3]

:~ chosen_strategy(direct_attack),border_to_enemy(_,P), enemy_strengthened(P). [8@2]
:~ chosen_strategy(expansion), systems_lost. [7@2]
:~ chosen_strategy(cooperative_attack), attackers_count(To, C), C < 3. [C*2@1]



#show send_fleet/3.
#show chosen_strategy/1.
#show applicable_strategy/1.
#show send_expansion_fleet/3.
#show send_defensive_fleet/3.
#show send_attack_fleet/3.
#show send_cooperative_fleet/3.
#show expansion_ships/3.
#show attack_ships/3.
#show cooperative_ships/3.
#show defensive_ships/3.
#show enemy/1.
#show enemy_system/2.
#show undirected_connected/2.
#show border_system/1.
#show my_system/1.
#show ships/2.
#show difficulty/1.

% #show reinforce_conditions/0.
% #show border_enemy_system_strongest/2.
% #show send_reinforce_fleet/3.
#show direct_attack_conditions/0.
#show direct_attack/3.
#show candidate_my_system_helper/1.
#show incoming_ships/2.
#show aggressive_cooperative_possible/1.
#show aggressive_attack_possible/2.