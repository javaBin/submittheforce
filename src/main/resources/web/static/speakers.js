jQuery(function() {
    $("body").on("htmx:load", function() {
        // Detect session form
        if ($("#talkForm").length == 1) {
            $("#speakers").on("htmx:afterSettle", updateSpeakers);
            $("#form-kind").on("change", kindChanged);
            updateSpeakers();
        }
    });
});

function updateSpeakers() {
    // Attach listener
    $(".speaker").on("htmx:afterSwap", updateSpeakers);

    // Update speaker part of form
    $(".speaker").each(function(i, el) {
        $(el).find("h2").text("Speaker " + (i + 1));
        $(el).find(":input").each(function(j, element) {
            var name = $(element).attr('name');
            if (name != undefined && name.startsWith("speakers[")) {
                var newName = "speakers[" + i + "]." + name.split('.')[1];
                $(element).attr('name', newName).attr('id', "form-" + newName);
                $(element).prev().attr('for', 'form-' + newName);
            }
        });
    });

    // Make sure to hide "Add speaker" button when there are enough speakers
    var maxAllowed = $('#form-kind').val().startsWith("WORKSHOP") ? 3 : 2;
    if ($(".speaker").length >= maxAllowed) {
        $(".addSpeaker").hide();
    } else {
        $(".addSpeaker").show();
    }

    // Hide "Remove speaker" button when only one speaker is present
    if ($(".speaker").length == 1) {
        $(".removeSpeaker").hide();
    } else {
        $(".removeSpeaker").show();
    }
}

function kindChanged() {
    console.log("Kind changed");
    if ($('#form-kind').val().startsWith('WORKSHOP')) {
        $('#form-kind-only').show();
    } else {
        $('#form-kind-only').hide();
    }

    updateSpeakers();
}