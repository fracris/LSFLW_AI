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
border_to_enemy(S, P) :- my_system(S), undirected_connected(S, N), enemy_system(N, P).
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

% ===== ANALISI SITUAZIONE STORICA =====
% Determina se un nemico si è rafforzato (solo se esistono dati precedenti)
enemy_strengthened(P) :-
    enemy(P),
    enemy_ships_total(P, Current),
    previous_enemy_ships_total(P, Previous),
    Current > Previous.

% Verifica perdite significative (2 o più sistemi)
significant_losses :- #count{S : system_lost(S)} >= 2.

% Opportunità di espansione in presenza di sistemi neutrali
neutral_opportunity :- neutral_system(_).

% Verifica se l'IA ha guadagnato sistemi
systems_gained :- system_gained(_).

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

% Verifica esistenza di condizioni di attacco diretto
has_direct_attack_target :- direct_attack_conditions(P), enemy(P).

% Verifica esistenza di condizioni di attacco cooperativo
has_cooperative_attack_target :- cooperative_attack_conditions(P), enemy(P).

% Condizioni per strategia di espansione
early_game :- my_system_count(C), C <= 3.
expansion_conditions :- early_game, neutral_opportunity.
expansion_conditions :- neutral_opportunity, not systems_lost.

% Condizioni per strategia difensiva
defensive_conditions :- systems_lost.
defensive_conditions :- significant_losses.

% Condizioni per attacco diretto
enemy_near(P) :-
    enemy(P),
    my_system(S),
    border_to_enemy(S,P).

direct_attack_conditions(P) :- enemy_near(P).

% Condizioni per attacco cooperativo
cooperative_target(Enemy_S, P) :-
    enemy_system(Enemy_S, P),
    #count{My_S : border_to_enemy(My_S, P), undirected_connected(My_S, Enemy_S)} =C,
    C>=2.

cooperative_attack_conditions(P) :- cooperative_target(_, P).

% Strategia di espansione applicabile
applicable_strategy(expansion) :- expansion_conditions.

% Strategia difensiva applicabile
applicable_strategy(defensive) :- defensive_conditions.

% Strategia di attacco diretto applicabile
applicable_strategy(direct_attack) :- has_direct_attack_target.

% Strategia di attacco cooperativo applicabile
applicable_strategy(cooperative_attack) :- has_cooperative_attack_target.

% Per tutti i livelli di difficoltà, seleziona esattamente una strategia
{ chosen_strategy(S) : applicable_strategy(S) } <= 1 :- difficulty(medium).
{ chosen_strategy(S) : applicable_strategy(S) } <= 2 :- difficulty(hard).

:~ #count{ S : chosen_strategy(S)} = C. [-C@2]

% ESPANSIONE: attacca sistemi neutrali vicini
target_neutral(From, To) :-
    chosen_strategy(expansion),
    my_system(From),
    neutral_system(To),
    undirected_connected(From, To),
    ships(From, Ships),
    Ships > 0.

% DIFENSIVA: rinforza sistemi di confine
reinforce_border(From, To) :-
    chosen_strategy(defensive),
    my_system(From),
    my_system(To),
    border_system(To),
    undirected_connected(From, To),
    ships(From, FromShips),
    ships(To, ToShips),
    FromShips > ToShips + 30,
    FromShips > 0.

direct_attack(From, To) :-
    chosen_strategy(direct_attack),
    my_system(From),
    enemy_system(To, P),
    undirected_connected(From, To),
    enemy_near(P),
    ships(From, FromShips),
    ships(To, ToShips),
    production(P,Pr),
    not flying_fleet(To,From),
    FromShips > ToShips + (80*Pr).

flying_fleet(F,T) :- fleet(_,_,_,F,T,_).

direct_attack(From, To) :-
    chosen_strategy(direct_attack),
    my_system(From),
    enemy_system(To, P),
    undirected_connected(From, To),
    enemy_near(P),
    ships(From, FromShips),
    ships(To, ToShips),
    production(P,Pr),
    fleet(F,_,Ships,To,From,_),
    FromShips > ToShips + (80*Pr) + Ships.

% Calcola le navi da inviare per ogni tipo di azione
expansion_ships(From, To, Ships) :-
    target_neutral(From, To),
    ships(From, TotalShips),
    Ships = (TotalShips * 3) / 4,
    Ships > 0,
    Ships < TotalShips.

defensive_ships(From, To, Ships) :-
    reinforce_border(From, To),
    ships(From, TotalShips),
    Ships = TotalShips / 2,
    Ships > 0,
    Ships < TotalShips.

attack_ships(From, To, Ships) :-
    direct_attack(From, To),
    ships(From, FromShips),
    ships(To, ToShips),
    production(To,P),
    Ships = ToShips + (80*P),
    Ships > 0,
    Ships < FromShips,
    not flying_fleet(To,From).

attack_ships(From, To, Ships) :-
    direct_attack(From, To),
    ships(From, FromShips),
    ships(To, ToShips),
    production(To,P),
    Ships = ToShips + (80*P) + Ships,
    Ships > 0,
    Ships < FromShips,
    fleet(F,_,Ships,To,From,_).

% Identifica tutti i sistemi che possono partecipare all'attacco coordinato di un bersaglio specifico
coordinated_attackers(From, To) :-
    chosen_strategy(cooperative_attack),
    my_system(From),
    enemy_system(To, P),
    cooperative_attack_conditions(P),
    undirected_connected(From, To),
    ships(From, FromShips),
    ships(To, ToShips),
    % Il sistema deve avere abbastanza navi per contribuire significativamente
    FromShips >= 20,
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

cooperative_ships(From, To, Ships) :-
    chosen_strategy(cooperative_attack),
    best_coordinated_target(To),
    coordinated_attackers(From, To),
    ships(From, TotalShips),
    ships(To, ToShips),
    production(To, ToProd),
    attackers_count(To, AttackerCount),
    % Calcola il contributo di questo sistema basato sul numero totale di attaccanti
    RequiredTotal = ToShips + (ToProd * 80),
    Ships = RequiredTotal / AttackerCount,
    Ships > 0,
    Ships <= (TotalShips * 2) / 3,
    Ships > 0.

% Identifica sistemi che partecipano all'attacco cooperativo allo stesso bersaglio
cooperative_participant(From, To) :-
    chosen_strategy(cooperative_attack),
    cooperative_attack(From, To).

% Conta partecipanti per ogni bersaglio
cooperative_participants_count(To, Count) :-
    chosen_strategy(cooperative_attack),
    enemy_system(To, _),
    #count{From : cooperative_participant(From, To)} = Count.

% Calcola navi solo per bersagli con almeno 2 partecipanti
cooperative_ships(From, To, Ships) :-
    cooperative_participant(From, To),
    cooperative_participants_count(To, Count),
    Count >= 2,
    ships(From, TotalShips),
    Ships = (TotalShips * 2) / 3,
    Ships > 0,
    Ships < TotalShips.

% ===== AZIONI FINALI CORRETTE =====
{send_expansion_fleet(From, To, Ships): expansion_ships(From, To, Ships)} = 1 :-
    chosen_strategy(expansion).

{send_defensive_fleet(From, To, Ships): defensive_ships(From, To, Ships)} = 1 :-
    chosen_strategy(defensive).

{send_attack_fleet(From, To, Ships): attack_ships(From, To, Ships)} = 1 :-
    chosen_strategy(direct_attack).

% Per attacco cooperativo, genera azioni solo per il bersaglio selezionato
{send_cooperative_fleet(From, To, Ships): cooperative_ships(From, To, Ships)} >= 2 :-
    chosen_strategy(cooperative_attack),
    best_coordinated_target(To).

% Evita conflitti tra attacco diretto e cooperativo
:- send_attack_fleet(F1, T, S1), send_cooperative_fleet(F2, T, S2).

% Assicurati che tutti gli attacchi cooperativi vadano allo stesso bersaglio
:- send_cooperative_fleet(F1, T1, S1), send_cooperative_fleet(F2, T2, S2), T1 != T2.

% Assicurati che ci siano almeno 2 sistemi nell'attacco cooperativo
:- chosen_strategy(cooperative_attack), #count{From : send_cooperative_fleet(From, _, _)} < 2.

send_fleet(From,To,Ships) :- send_expansion_fleet(From,To,Ships).
send_fleet(From,To,Ships) :- send_defensive_fleet(From,To,Ships).
send_fleet(From,To,Ships) :- send_attack_fleet(From,To,Ships).
send_fleet(From,To,Ships) :- send_cooperative_fleet(From,To,Ships).

% Evita azioni che lascerebbero sistemi vulnerabili
:- send_fleet(From, _, Ships), ships(From, Total), Ships >= Total.

% Assicurati che le navi inviate siano positive
:- send_fleet(_, _, Ships), Ships <= 0.

% Preferisci azioni che utilizzano sistemi con più navi
:~ send_fleet(From, _, _), ships(From, Ships). [10 - Ships@1]

% Preferisci attaccare sistemi nemici più deboli
:~ send_fleet(_, To, _), enemy_system(To, _), ships(To, Ships). [Ships@1]

#show send_fleet/3.
#show chosen_strategy/1.
#show applicable_strategy/1.
#show enemy/1.
#show enemy_system/2.
#show undirected_connected/2.
#show border_system/1.
#show my_system/1.
#show ships/2.
#show difficulty/1.