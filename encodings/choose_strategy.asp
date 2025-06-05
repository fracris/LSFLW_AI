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

% ===== PREDICATI AUSILIARI PER GESTIRE LA NEGAZIONE =====
% Verifica esistenza di condizioni di attacco diretto
has_direct_attack_target :- direct_attack_conditions(P), enemy(P).

% Verifica esistenza di condizioni di attacco cooperativo
has_cooperative_attack_target :- cooperative_attack_conditions(P), enemy(P).

% ===== CONDIZIONI STRATEGICHE =====
% Condizioni per strategia di espansione
early_game :- my_system_count(C), C <= 3.
expansion_conditions :- early_game, neutral_opportunity.
expansion_conditions :- neutral_opportunity, not systems_lost.

% Condizioni per strategia difensiva
defensive_conditions :- systems_lost.
defensive_conditions :- significant_losses.

% Condizioni per attacco diretto
weak_enemy(P) :-
    enemy(P),
    my_ships_total(My),
    enemy_ships_total(P, Enemy),
    My > Enemy + 50.

direct_attack_conditions(P) :- weak_enemy(P).

% Condizioni per attacco cooperativo
cooperative_target(Enemy_S, P) :-
    enemy_system(Enemy_S, P),
    #count{My_S : border_to_enemy(My_S, P), undirected_connected(My_S, Enemy_S)} >= 2.

cooperative_attack_conditions(P) :- cooperative_target(_, P).

% ===== APPLICABILITÀ STRATEGIE =====
% Strategia di espansione applicabile
applicable_strategy(expansion) :- expansion_conditions.

% Strategia difensiva applicabile
applicable_strategy(defensive) :- defensive_conditions.

% Strategia di attacco diretto applicabile
applicable_strategy(direct_attack) :- has_direct_attack_target.

% Strategia di attacco cooperativo applicabile
applicable_strategy(cooperative_attack) :- has_cooperative_attack_target.

% Strategia di default - CORRETTA per DLV 2.1
applicable_strategy(expansion) :-
    not defensive_conditions,
    not has_direct_attack_target,
    not has_cooperative_attack_target.

% ===== SELEZIONE STRATEGIE =====
% Per tutti i livelli di difficoltà, seleziona esattamente una strategia
 { chosen_strategy(S) : applicable_strategy(S) }  = 1.

% ===== IMPLEMENTAZIONE AZIONI STRATEGICHE =====
% ESPANSIONE: attacca sistemi neutrali vicini
target_neutral(From, To) :-
    chosen_strategy(expansion),
    my_system(From),
    neutral_system(To),
    undirected_connected(From, To),
    ships(From, Ships),
    Ships > 5.

% DIFENSIVA: rinforza sistemi di confine
reinforce_border(From, To) :-
    chosen_strategy(defensive),
    my_system(From),
    my_system(To),
    border_system(To),
    undirected_connected(From, To),
    ships(From, FromShips),
    ships(To, ToShips),
    FromShips > ToShips + 3,
    FromShips > 5.

% ATTACCO DIRETTO: attacca nemico debole
direct_attack(From, To) :-
    chosen_strategy(direct_attack),
    my_system(From),
    enemy_system(To, P),
    weak_enemy(P),
    undirected_connected(From, To),
    ships(From, FromShips),
    ships(To, ToShips),
    FromShips > ToShips + 2.

% ATTACCO COOPERATIVO: coordina attacchi multipli
cooperative_attack(From, To) :-
    chosen_strategy(cooperative_attack),
    my_system(From),
    enemy_system(To, P),
    cooperative_target(To, P),
    undirected_connected(From, To),
    ships(From, Ships),
    Ships > 3.

% ===== GENERAZIONE AZIONI SEND_FLEET =====
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
    Ships = (TotalShips * 1) / 2,
    Ships > 0,
    Ships < TotalShips.

attack_ships(From, To, Ships) :-
    direct_attack(From, To),
    ships(From, FromShips),
    ships(To, ToShips),
    Ships = ToShips + 5,
    Ships > 0,
    Ships < FromShips.

cooperative_ships(From, To, Ships) :-
    cooperative_attack(From, To),
    ships(From, TotalShips),
    Ships = (TotalShips * 2) / 3,
    Ships > 0,
    Ships < TotalShips.

% ===== AZIONI FINALI =====
% Genera le azioni send_fleet
send_fleet(From, To, Ships) :- expansion_ships(From, To, Ships).
send_fleet(From, To, Ships) :- defensive_ships(From, To, Ships).
send_fleet(From, To, Ships) :- attack_ships(From, To, Ships).
send_fleet(From, To, Ships) :- cooperative_ships(From, To, Ships).

% ===== VINCOLI DI SICUREZZA =====
% Evita azioni che lascerebbero sistemi vulnerabili
:- send_fleet(From, _, Ships), ships(From, Total), Ships >= Total.

% Assicurati che le navi inviate siano positive
:- send_fleet(_, _, Ships), Ships <= 0.

% Limita il numero di azioni per livello (opzionale)
:- #count{From, To, Ships : send_fleet(From, To, Ships)} > 3.

% ===== OTTIMIZZAZIONI =====
% Preferisci azioni che utilizzano sistemi con più navi
:~ send_fleet(From, _, _), ships(From, Ships). [10 - Ships@1]

% Preferisci attaccare sistemi nemici più deboli
:~ send_fleet(_, To, _), enemy_system(To, _), ships(To, Ships). [Ships@1]

#show send_fleet/3.
#show chosen_strategy/1.