console.log("this is script file")

const toggleSidebar = () => {

	if ($(".sidebar").is(":visible")) {
		//true
		// Hide the slide bar

		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");

	} else {
		//false
		// show the slidebar

		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}

};

const search = () => {
    // Get the search query from the input field
    let query = $("#search-input").val();

    // When the search is blank, then hide the search box
    if (query === "") {
        $(".search-result").hide();
    } else {
        // Sending request to server (backend)
        const url = `http://localhost:8080/search/${query}`;

        fetch(url)
            .then((response) => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then((data) => {
                // Create the search result HTML
                let text = `<div class="list-group">`;

                data.forEach((contact) => {
                    text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`;
                });

                text += `</div>`;

                // Update the search result container and show it
                $(".search-result").html(text);
                $(".search-result").show();
            })
            .catch((error) => {
                console.error('There was a problem with the fetch operation:', error);
            });
    }
};