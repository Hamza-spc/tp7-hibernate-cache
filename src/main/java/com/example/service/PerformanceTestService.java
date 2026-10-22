package com.example.service;

import com.example.model.Auteur;
import java.util.List;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

public class PerformanceTestService {

    public EntityManager em;

    public void testPerformanceSansCache() {
        em.clear();
        long requetesAvant = statistiques().getPrepareStatementCount();

        em.getTransaction().begin();
        long start = System.currentTimeMillis();

        TypedQuery<Auteur> query = em.createQuery("SELECT a FROM Auteur a", Auteur.class);
        List<Auteur> auteurs = query.getResultList();

        // Chaque acces a la collection declenche une requete : probleme N+1
        for (Auteur auteur : auteurs) {
            auteur.getLivres().size();
        }

        long end = System.currentTimeMillis();
        em.getTransaction().commit();

        System.out.println("Durée sans cache : " + (end - start) + " ms");
        afficherStatistiques(requetesAvant);
    }

    public void testPerformanceAvecJoinFetch() {
        em.clear();
        long requetesAvant = statistiques().getPrepareStatementCount();

        em.getTransaction().begin();
        long start = System.currentTimeMillis();

        // DISTINCT evite les doublons dus a la jointure
        TypedQuery<Auteur> query = em.createQuery(
                "SELECT DISTINCT a FROM Auteur a JOIN FETCH a.livres", Auteur.class);
        List<Auteur> auteurs = query.getResultList();

        // L'acces aux livres ne declenche plus de requete supplementaire
        for (Auteur auteur : auteurs) {
            auteur.getLivres().size();
        }

        long end = System.currentTimeMillis();
        em.getTransaction().commit();

        System.out.println("Durée avec JOIN FETCH : " + (end - start) + " ms");
        afficherStatistiques(requetesAvant);
    }

    public void testPerformanceAvecEntityGraph() {
        em.clear();
        long requetesAvant = statistiques().getPrepareStatementCount();

        em.getTransaction().begin();
        long start = System.currentTimeMillis();

        EntityGraph<?> graph = em.createEntityGraph("Auteur.livres");
        TypedQuery<Auteur> query = em.createQuery("SELECT a FROM Auteur a", Auteur.class);
        query.setHint("javax.persistence.fetchgraph", graph);
        List<Auteur> auteurs = query.getResultList();

        for (Auteur auteur : auteurs) {
            auteur.getLivres().size();
        }

        long end = System.currentTimeMillis();
        em.getTransaction().commit();

        System.out.println("Durée avec Entity Graph : " + (end - start) + " ms");
        afficherStatistiques(requetesAvant);
    }

    public void testPerformanceAvecCache() {
        // Premiere lecture : chargement depuis la base et remplissage des caches
        lectureEnCache(1);
        // Lectures suivantes : servies par le cache de second niveau
        lectureEnCache(2);
        lectureEnCache(3);
    }

    private void lectureEnCache(int numero) {
        em.clear();
        long requetesAvant = statistiques().getPrepareStatementCount();

        em.getTransaction().begin();
        long start = System.currentTimeMillis();

        TypedQuery<Auteur> query = em.createQuery(
                "SELECT DISTINCT a FROM Auteur a JOIN FETCH a.livres", Auteur.class);
        query.setHint("org.hibernate.cacheable", true);
        List<Auteur> auteurs = query.getResultList();
        for (Auteur auteur : auteurs) {
            auteur.getLivres().size();
        }

        long end = System.currentTimeMillis();
        em.getTransaction().commit();

        System.out.println("Durée avec cache (lecture " + numero + ") : " + (end - start) + " ms");
        afficherStatistiques(requetesAvant);
    }

    private Statistics statistiques() {
        return em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    }

    private void afficherStatistiques(long requetesAvant) {
        Statistics statistiques = statistiques();
        System.out.println("Requêtes SQL exécutées : "
                + (statistiques.getPrepareStatementCount() - requetesAvant)
                + " | cache 2e niveau : hits=" + statistiques.getSecondLevelCacheHitCount()
                + ", miss=" + statistiques.getSecondLevelCacheMissCount()
                + " | cache de requêtes : hits=" + statistiques.getQueryCacheHitCount());
    }
}
