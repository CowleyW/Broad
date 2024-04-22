# Broad Institute Take Home Assignment

Completed by William Cowley

---

### API Design Choice

I chose to allow the MBTA server to filter the data for me. Since we are using the filter functionality in a very straight-forward manner, it seems more productive to re-use the api's provided filtering, 
rather than to re-implement it ourselves. If we had a more complicated way of filtering, I would choose to do it all client-side to avoid fragmenting filtering logic
