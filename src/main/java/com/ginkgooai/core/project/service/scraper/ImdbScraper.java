package com.ginkgooai.core.project.service.scraper;

import com.ginkgooai.core.project.domain.talent.ImdbMovieItem;
import com.ginkgooai.core.project.domain.talent.TalentProfileMeta;
import com.ginkgooai.core.project.dto.TalentProfileData;
import com.ginkgooai.core.project.repository.ImdbMovieItemRepository;
import com.ginkgooai.core.project.repository.TalentProfileMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImdbScraper {

    private final TalentProfileMetaRepository talentProfileMetaRepository;
    private final ImdbMovieItemRepository imdbMovieItemRepository;

    public TalentProfileMeta scrapeProfile(String imdbUrl) throws Exception {
        log.info("Scraping profile from: {}", imdbUrl);
      
        //TODO: Remove this block when needed
        if (true) {
            return TalentProfileMeta.builder()
                    .source("IMDB")
                    .sourceUrl(imdbUrl)
                    .build();
        }

        Document doc = Jsoup.connect(imdbUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000)
                .get();

        // Extract name using the updated selectors
        String name = "";
        Element nameElement = doc.selectFirst("span[data-testid='hero__primary-text']");
        if (nameElement != null) {
            name = nameElement.text().trim();
        } else {
            // Fallback to older selectors
            name = doc.select("h1.sc-afe43def-0").text();
            if (name.isEmpty()) {
                name = doc.select("h1.header").text();
            }
            if (name.isEmpty()) {
                name = doc.select("span.itemprop[itemprop=name]").text();
            }
        }
        log.info("Found name: {}", name);

        // Extract name suffix (birth/death years)
        String nameSuffix = extractNameSuffix(doc);
        log.info("Found name suffix: {}", nameSuffix);

        // Extract biography
        String bio = doc.select("div.name-trivia-bio-text").text();
        if (bio.isEmpty()) {
            bio = doc.select("div.inline").text();
        }
        if (bio.isEmpty()) {
            // Try newer biography selector
            bio = doc.select("div.ipc-html-content-inner-div").text();
        }
        log.info("Found bio excerpt: {}", bio.length() > 50 ? bio.substring(0, 50) + "..." : bio);

        // Extract skills/professions
        Set<String> skills = new HashSet<>();
        Elements skillElements = doc.select("a[href*=profession]");
        if (skillElements.isEmpty()) {
            // Try newer skills selector
            skillElements = doc.select("span.ipc-chip__text");
        }
        skillElements.forEach(element ->
                skills.add(element.text())
        );
        log.info("Found skills: {}", skills);

        // Extract known for works
        Set<ImdbMovieItem> knownFor = extractKnownFor(doc);
        log.info("Found known for: {}", knownFor);

        // Extract photo URL - Updated to handle the new structure
        String photoUrl = extractPhotoUrl(doc);
        log.info("Found photo URL: {}", photoUrl);

        // Extract personal details
        Map<String, String> personalDetails = scrapePersonalDetails(doc);
        log.info("Found personal details: {}", personalDetails);

        // Update or create movie items
        Set<ImdbMovieItem> movieItems = handleMovieItems(knownFor);

        TalentProfileData profile = TalentProfileData.builder()
                .name(name)
                .nameSuffix(nameSuffix)
                .knownFor(knownFor)
                .photoUrl(photoUrl)
                .personalDetails(personalDetails)
                .source("IMDB")
                .sourceUrl(imdbUrl)
                .build();


        // First check if profile already exists
        Optional<TalentProfileMeta> existingProfile = talentProfileMetaRepository.findBySourceUrl(imdbUrl);
        TalentProfileMeta profileMeta = talentProfileMetaRepository.save(TalentProfileMeta.builder()
                .id(existingProfile.map(TalentProfileMeta::getId).orElse(null))
                .source("IMDB")
                .sourceUrl(imdbUrl)
                .data(profile)
                .build());

        return profileMeta;
    }

    private Set<ImdbMovieItem> handleMovieItems(Set<ImdbMovieItem> newItems) {
        if (newItems.isEmpty()) {
            return newItems;
        }
        
        // Get all titleUrls from new items
        Set<String> newTitleUrls = newItems.stream()
                .map(ImdbMovieItem::getTitleUrl)
                .collect(Collectors.toSet());

        // Find existing titleUrls
        Set<String> existingTitleUrls = imdbMovieItemRepository.findExistingTitleUrls(newTitleUrls);

        // Filter out items that already exist
        Set<ImdbMovieItem> itemsToSave = newItems.stream()
                .filter(item -> !existingTitleUrls.contains(item.getTitleUrl()))
                .collect(Collectors.toSet());

        if (!itemsToSave.isEmpty()) {
            log.info("Saving {} new movie items", itemsToSave.size());
            imdbMovieItemRepository.saveAll(itemsToSave);
        }

        // Return all items (both new and existing)
        return newItems;
    }

    /**
     * Extract name suffix information, including birth and death details
     */
    private String extractNameSuffix(Document doc) {
        StringBuilder suffix = new StringBuilder();

        // First try to extract suffix from the original way (if any)
        Element nameElement = doc.selectFirst("h1.header span.nobr");
        if (nameElement != null) {
            String nameSuffix = nameElement.text().trim();
            if (!nameSuffix.isEmpty()) {
                suffix.append(nameSuffix);
            }
        }

        // Try to extract from birth and death information
        Element birthAndDeathSection = doc.selectFirst("aside[data-testid='birth-and-death-section']");
        if (birthAndDeathSection != null) {
            // Extract birth date
            Element birthElement = birthAndDeathSection.selectFirst("div[data-testid='birth-and-death-birthdate']");
            if (birthElement != null) {
                Element birthDateElement = birthElement.selectFirst("span.sc-59a43f1c-2:nth-child(2)");
                if (birthDateElement != null) {
                    String birthDate = birthDateElement.text().trim();
                    if (!birthDate.isEmpty()) {
                        if (suffix.length() > 0) {
                            suffix.append(", ");
                        }
                        suffix.append("").append(birthDate);
                    }
                }
            }

            // Extract death date and age
            Element deathElement = birthAndDeathSection.selectFirst("div[data-testid='birth-and-death-deathdate']");
            if (deathElement != null) {
                Element deathDateElement = deathElement.selectFirst("span.sc-59a43f1c-2:nth-child(2)");
                if (deathDateElement != null) {
                    String deathInfo = deathDateElement.ownText().trim(); // Get only text, excluding child elements

                    // Extract death age
                    Element ageElement = deathElement.selectFirst("span[data-testid='birth-and-death-death-age']");
                    String age = "";
                    if (ageElement != null) {
                        age = ageElement.text().trim();
                    }

                    if (!deathInfo.isEmpty()) {
                        if (suffix.length() > 0) {
                            suffix.append(", ");
                        }
                        suffix.append("-").append(deathInfo);
                        if (!age.isEmpty()) {
                            suffix.append(" ").append(age);
                        }
                    }
                }
            }
        }

        // If no birth/death information found, try to extract from old layout
        if (suffix.length() == 0) {
            Elements bioElements = doc.select("div#name-bio-text div.inline");
            for (Element element : bioElements) {
                String text = element.text().trim();
                if (text.contains("Born:") || text.contains("Died:")) {
                    if (suffix.length() > 0) {
                        suffix.append(", ");
                    }
                    suffix.append(text);
                }
            }
        }

        return suffix.length() > 0 ? suffix.toString() : null;
    }


    /**
     * Extract photo URL from various possible locations in the document
     */
    private String extractPhotoUrl(Document doc) {
        // Try to find the image URL from various possible locations

        // 1. Try the new structure with ipc-lockup-overlay
        Element photoLinkElement = doc.selectFirst("a.ipc-lockup-overlay[href*='mediaviewer']");
        if (photoLinkElement != null) {
            // The actual image might be near this element
            Element parentElement = photoLinkElement.parent();
            if (parentElement != null) {
                Element imgElement = parentElement.selectFirst("img");
                if (imgElement != null) {
                    String src = imgElement.attr("src");
                    if (!src.isEmpty()) {
                        return src;
                    }
                }
            }
        }

        // 2. Try direct img selection with various classes
        Element photoElement = doc.selectFirst("img.ipc-image");
        if (photoElement != null) {
            String src = photoElement.attr("src");
            if (!src.isEmpty()) {
                return src;
            }
        }

        // 3. Try the older poster class
        photoElement = doc.selectFirst("img.poster");
        if (photoElement != null) {
            String src = photoElement.attr("src");
            if (!src.isEmpty()) {
                return src;
            }
        }

        // 4. Try to find any image in the hero section
        Element heroSection = doc.selectFirst("div[data-testid='hero-media__poster']");
        if (heroSection != null) {
            Element imgElement = heroSection.selectFirst("img");
            if (imgElement != null) {
                String src = imgElement.attr("src");
                if (!src.isEmpty()) {
                    return src;
                }
            }
        }

        // 5. Last resort: try to find any profile image
        Elements allImages = doc.select("img[alt*='" + doc.title().split("- IMDb")[0].trim() + "']");
        if (!allImages.isEmpty()) {
            String src = allImages.first().attr("src");
            if (!src.isEmpty()) {
                return src;
            }
        }

        return ""; // Return empty string if no image found
    }

    /**
     * "Known For"
     */
    private Set<ImdbMovieItem> extractKnownFor(Document doc) {
        Set<ImdbMovieItem> knownFor = new HashSet<>();

        Elements knownForCards = doc.select("div[data-testid^='nm_kwn_for_']");

        if (!knownForCards.isEmpty()) {
            for (Element card : knownForCards) {
                String title = "";
                String cover = "";
                String role = "";
                String year = "";
                String rating = "";
                String mediaType = "";
                String titleUrl = "";

                Element titleElement = card.selectFirst("a.ipc-primary-image-list-card__title");
                if (titleElement != null) {
                    title = titleElement.text().trim();
                    titleUrl = titleElement.attr("href");
                    if (titleUrl.contains("?")) {
                        titleUrl = titleUrl.substring(0, titleUrl.indexOf("?"));
                    }
                }

                // Extract cover image URL
                Element coverElement = card.selectFirst("img.ipc-image");
                if (coverElement != null) {
                    cover = coverElement.attr("src");
                }

                // Alternative way to find cover image
                if (cover.isEmpty()) {
                    Element overlayElement = card.selectFirst("a.ipc-lockup-overlay");
                    if (overlayElement != null) {
                        // Find the closest image to this overlay
                        Element imgElement = card.selectFirst("img");
                        if (imgElement != null) {
                            cover = imgElement.attr("src");
                        }
                    }
                }

                Element roleElement = card.selectFirst("span.ipc-primary-image-list-card__secondary-text");
                if (roleElement != null) {
                    role = roleElement.text().trim();
                }

                Element yearElement = card.selectFirst("span[data-testid^='nm-flmg-title-year-']");
                if (yearElement != null) {
                    year = yearElement.text().trim();
                }

                Element ratingElement = card.selectFirst("span.ipc-rating-star--rating");
                if (ratingElement != null) {
                    rating = ratingElement.text().trim();
                }

                Element mediaTypeElement = card.selectFirst("div.ipc-primary-image-list-card__title-type");
                if (mediaTypeElement != null) {
                    mediaType = mediaTypeElement.text().trim();
                }

                if (!title.isEmpty()) {
                    ImdbMovieItem item = new ImdbMovieItem();
                    item.setTitle(title);
                    item.setRole(role);
                    item.setYear(year);
                    item.setRating(rating);
                    item.setMediaType(mediaType);
                    item.setTitleUrl(titleUrl);
                    item.setCover(cover); // Set the cover image URL
                    knownFor.add(item);

                    log.debug("添加Known For项: {} ({}), 角色: {}, 评分: {}, 类型: {}, 封面: {}",
                            title, year, role, rating, mediaType, cover);
                }
            }
        } else {
            Elements knownForElements = doc.select("div.knownfor-title-role a.knownfor-ellipsis");
            if (knownForElements.isEmpty()) {
                knownForElements = doc.select("div.title a");
            }

            for (Element element : knownForElements) {
                String title = element.text().trim();
                String titleUrl = element.attr("href");

                // Try to find cover image in older layout
                String cover = "";
                Element parent = element.parent();
                if (parent != null) {
                    Element imgElement = parent.selectFirst("img");
                    if (imgElement != null) {
                        cover = imgElement.attr("src");
                    }
                }

                if (!title.isEmpty()) {
                    ImdbMovieItem item = new ImdbMovieItem();
                    item.setTitle(title);
                    item.setTitleUrl(titleUrl);
                    item.setCover(cover);
                    knownFor.add(item);
                }
            }
        }

        return knownFor;
    }


    /**
     * Scrape the personal details section
     */
    private Map<String, String> scrapePersonalDetails(Document doc) {
        Map<String, String> details = new HashMap<>();

        // Locate the personal details section
        Element personalDetailsSection = doc.selectFirst("section[data-testid='PersonalDetails']");
        if (personalDetailsSection == null) {
            log.warn("Personal details section not found");
            return details;
        }

        // Get all list items
        Elements listItems = personalDetailsSection.select("li.ipc-metadata-list__item");

        for (Element item : listItems) {
            Element labelElement = item.selectFirst(".ipc-metadata-list-item__label");
            if (labelElement == null) continue;

            String label = labelElement.text().trim();

            // Process different content based on label
            switch (label) {
                case "Born":
                    StringBuilder birthInfo = new StringBuilder();
                    Elements birthDateElements = item.select(".ipc-inline-list__item a");
                    for (Element elem : birthDateElements) {
                        birthInfo.append(elem.text().trim()).append(" ");
                    }

                    Element birthPlace = item.selectFirst("a[href*='birth_place']");
                    if (birthPlace != null) {
                        birthInfo.append(birthPlace.text().trim());
                    }
                    details.put("born", birthInfo.toString().trim());
                    break;

                case "Died":
                    StringBuilder deathInfo = new StringBuilder();
                    Elements deathElements = item.select(".ipc-inline-list__item a");
                    for (Element elem : deathElements) {
                        deathInfo.append(elem.text().trim()).append(" ");
                    }

                    Element deathCause = item.selectFirst(".ipc-metadata-list-item__list-content-item--subText");
                    if (deathCause != null) {
                        deathInfo.append("(").append(deathCause.text().trim()).append(")");
                    }
                    details.put("died", deathInfo.toString().trim());
                    break;

                case "Height":
                    Element heightElem = item.selectFirst(".ipc-metadata-list-item__list-content-item");
                    if (heightElem != null) {
                        details.put("height", heightElem.text().trim());
                    }
                    break;

                case "Spouse":
                    Element spouseElem = item.selectFirst(".ipc-metadata-list-item__list-content-item--link");
                    Element spouseDate = item.selectFirst(".ipc-metadata-list-item__list-content-item--subText");
                    if (spouseElem != null) {
                        String spouseInfo = spouseElem.text().trim();
                        if (spouseDate != null) {
                            spouseInfo += " (" + spouseDate.text().trim() + ")";
                        }
                        details.put("spouse", spouseInfo);
                    }
                    break;

                case "Alternative name":
                case "Alternative names":
                    Element altNameElem = item.selectFirst(".ipc-metadata-list-item__list-content-item");
                    if (altNameElem != null) {
                        details.put("alternativeName", altNameElem.text().trim());
                    }
                    break;

                case "Children":
                    Element childrenElem = item.selectFirst(".ipc-metadata-list-item__list-content-item");
                    if (childrenElem != null) {
                        details.put("children", childrenElem.text().trim());
                    }
                    break;

                case "Parents":
                    Element parentsElem = item.selectFirst(".ipc-metadata-list-item__list-content-item");
                    if (parentsElem != null) {
                        details.put("parents", parentsElem.text().trim());
                    }
                    break;

                case "Relatives":
                    Element relativesElem = item.selectFirst(".ipc-metadata-list-item__list-content-item");
                    if (relativesElem != null) {
                        String relativesInfo = relativesElem.text().trim();
                        Element relativesSubtext = item.selectFirst(".ipc-metadata-list-item__list-content-item--subText");
                        if (relativesSubtext != null) {
                            relativesInfo += " (" + relativesSubtext.text().trim() + ")";
                        }
                        details.put("relatives", relativesInfo);
                    }
                    break;
            }
        }

        return details;
    }
}
