$.getJSON("user", function (data) {
    let isUserAdmin = false;
    $("#user-name-span").append(data.name);

    for (let i = 0; i < data.roles.length; i++) {
        if (data.roles[i] === "ROLE_ADMIN") {
            $("#admin-button-div").removeAttr("hidden");
            isUserAdmin = true;
        }
        $("#user-roles-span").append(data.roles[i].replace("ROLE_", " "));
    }

    if (!isUserAdmin) {
        $("#user-button-div").click();
    } else {
        $("#table-div").load("admin/page #admin-page-div");
        parseAllUsers();
    }
});

$(document).ready(function () {
    $("#user-button-div").click(function pressUserButton() {
        if ($("#user-button-div").is('.active')) {
            $("#table-div").load("user/page #user-page-div");
            $.getJSON("user", function (data) {
                parseCurrJsonUser(data)
            });
            $(this).toggleClass('text-white text-primary bg-primary active');
            $("#admin-button-div").toggleClass('text-white text-primary bg-primary');
            if (!$("#admin-button-div").is('.active')) {
                $("#admin-button-div").toggleClass('active')
            }
        }
    }).hover(function () {
        $(this).toggleClass('text-primary text-white bg-dark');
    });
});

$(document).ready(function () {
    $("#admin-button-div").hover(function () {
        $(this).toggleClass('text-primary text-white bg-dark');
    }).click(function () {
        if ($("#admin-button-div").is('.active')) {
            $("#table-div").load("admin/page #admin-page-div");
            parseAllUsers();
            $(this).toggleClass('text-white text-primary bg-primary active');
            $("#user-button-div").toggleClass('text-white text-primary bg-primary');
            if (!$("#user-button-div").is('.active')) {
                $("#user-button-div").toggleClass('active')
            }
        }
    });
    $("#modal-edit-button").click(function () {
        if ($("#modal-edit-button").text().includes("Сохранить")) {
            let object = {};
            object.id = $("#modal-input-id").val();
            object.name = $("#modal-input-name").val();
            object.password = $("#modal-input-password").val();
            object.email = $("#modal-input-email").val();
            object.age = $("#modal-input-age").val();
            object.telegramChatId = $("#modal-input-chatid").val();
            object.ruvdsApiToken = $("#modal-input-apitoken").val();
            object.minBalanceThreshold = $("#modal-input-minbalance").val();
            object.notificationEnabled = $("#modal-input-notification").is(":checked");
            object.roles = [];
            if ($("#modal-input-role-user").is(":checked")) {
                object.roles.push("ROLE_USER");
            }
            if ($("#modal-input-role-admin").is(":checked")) {
                object.roles.push("ROLE_ADMIN");
            }
            saveUser(object);
        } else {
            deleteUser($("#modal-input-id").val());
        }
    });
});

// NEW
$(document).ready(function() {
    // Загрузка текущих настроек пользователя при открытии страницы
    $.getJSON("user/settings", function(settings) {
        if (settings) {
            $("#telegramChatId").val(settings.telegramChatId || "");
            $("#ruvdsApiToken").val(settings.ruvdsApiToken || "");
            $("#minBalanceThreshold").val(settings.minBalanceThreshold || "");
            $("#notificationEnabled").prop('checked', settings.notificationEnabled || false);
        }
    });

    // Обработчик кнопки сохранения
    $("#saveSettings").click(function() {
        let settings = {
            telegramChatId: $("#telegramChatId").val() ? parseInt($("#telegramChatId").val()) : null,
            ruvdsApiToken: $("#ruvdsApiToken").val(),
            minBalanceThreshold: $("#minBalanceThreshold").val() ? parseFloat($("#minBalanceThreshold").val()) : null,
            notificationEnabled: $("#notificationEnabled").is(":checked")
        };

        $.ajax({
            url: "user/settings",
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(settings),
            success: function() {
                alert("Настройки успешно сохранены");
            },
            error: function(xhr, status, error) {
                console.error("Ошибка при сохранении настроек: ", error);
                alert("Ошибка при сохранении настроек: " + error);
            }
        });
    });
});

function parseCurrJsonUser(user) {
    let tr = [];
    tr.push("<tr>");
    tr.push("<td>" + user.id + "</td>");
    tr.push("<td>" + user.name + "</td>");
    tr.push("<td>" + user.email + "</td>");
    tr.push("<td>" + user.age + "</td>");
    tr.push("<td>");
    for (let i = 0; i < user.roles.length; i++) {
        tr.push(user.roles[i].replace("ROLE_", " "));
    }
    tr.push("</td>");
    tr.push("</tr>");
    $("#user-page-table-tbody").append(tr.join(''));

    // Обновленные проверки полей
    if (user.telegramChatId !== null && user.telegramChatId !== undefined) {
        $("#telegramChatId").val(user.telegramChatId);
    }
    if (user.ruvdsApiToken !== null && user.ruvdsApiToken !== undefined) {
        $("#ruvdsApiToken").val(user.ruvdsApiToken);
    }
    if (user.minBalanceThreshold !== null && user.minBalanceThreshold !== undefined) {
        $("#minBalanceThreshold").val(user.minBalanceThreshold);
    }
    if (user.notificationEnabled !== null && user.notificationEnabled !== undefined) {
        $("#notificationEnabled").prop('checked', user.notificationEnabled);
    }
}

function parseJsonUserForAdmin(user) {
    let tr = [];
    tr.push("<tr class=\"border-top\">");
    tr.push("<td>" + user.id + "</td>");
    tr.push("<td>" + user.name + "</td>");
    tr.push("<td>" + user.email + "</td>");
    tr.push("<td>" + user.age + "</td>");
    tr.push("<td>");
    for (let i = 0; i < user.roles.length; i++) {
        tr.push(user.roles[i].replace("ROLE_", " "));
    }
    tr.push("</td>");
    tr.push('<td> <input class=\"btn border-0 text-white bg-warning\" type=\"button" data-bs-toggle="modal" data-bs-target="#edit-modal" onclick="editUser('
        + user.id
        + ');" value="Изменить"></td>');
    tr.push('<td> <input class=\"btn border-0 text-white bg-danger\" type=\"button" data-bs-toggle="modal" data-bs-target="#edit-modal" onclick="deleteUserButton('
        + user.id
        + ');" value="Удалить"></td>');
    tr.push("</tr>");
    $("#admin-page-table-tbody").append(tr.join(''));
}

function parseAllUsers() {
    $.getJSON("admin/users", function (jsons) {
        $("#admin-page-table-tbody").empty();
        for (let i = 0; i < jsons.length; i++) {
            parseJsonUserForAdmin(jsons[i]);
        }
    });
}

function editUser(id) {
    $.getJSON("admin/user/" + id, function (user) {
        $('#modal-h2').text("Форма пользователя");
        $("#modal-input-id").val(user.id);
        $("#modal-input-name").val(user.name).prop('disabled', false);
        $("#modal-input-password").val(user.password).prop('disabled', false);
        $("#modal-input-email").val(user.email).prop('disabled', false);
        $("#modal-input-age").val(user.age).prop('disabled', false);
        $("#modal-input-chatid").val(user.telegramChatId).prop('disabled', false);
        $("#modal-input-apitoken").val(user.ruvdsApiToken).prop('disabled', false);
        $("#modal-input-minbalance").val(user.minBalanceThreshold).prop('disabled', false);
        $("#modal-input-notification").prop('checked', user.notificationEnabled || false).prop('disabled', false);
        $("#modal-input-role-user, #modal-input-role-admin")
            .prop('checked', false)
            .prop('disabled', false);

        for (let i = 0; i < user.roles.length; i++) {
            if (user.roles[i] === "ROLE_USER") {
                $("#modal-input-role-user").prop('checked', true);
            }
            if (user.roles[i] === "ROLE_ADMIN") {
                $("#modal-input-role-admin").prop('checked', true);
            }
        }

        $("#modal-edit-button").text("Сохранить").addClass("btn-primary").removeClass("btn-danger");
    });
}

function deleteUserButton(id) {
    $.getJSON("admin/user/" + id, function (user) {
        $('#modal-h2').text("Форма пользователя");
        $("#modal-input-id").val(user.id);
        $("#modal-input-name").val(user.name).prop('disabled', true);
        $("#modal-input-password").val(user.password).prop('disabled', true);
        $("#modal-input-email").val(user.email).prop('disabled', true);
        $("#modal-input-age").val(user.age).prop('disabled', true);
        $("#modal-input-chatid").val(user.telegramChatId).prop('disabled', true);
        $("#modal-input-apitoken").val(user.ruvdsApiToken).prop('disabled', true);
        $("#modal-input-minbalance").val(user.minBalanceThreshold).prop('disabled', true);
        $("#modal-input-notification").prop('checked', false).prop('disabled', true);
        $("#modal-input-role").prop('disabled', true);
        $("#modal-input-role-user, #modal-input-role-admin")
            .prop('checked', false)
            .prop('disabled', true);
        $("#modal-edit-button").text("Удалить").removeClass("btn-primary").addClass("btn-danger");
    });
}

function deleteUser(id) {
    $.ajax({
        url: "admin/user/" + id,
        type: 'DELETE',
        success: function () {
            parseAllUsers();
        },
        error: function (xhr, status, error) {
            console.error("Ошибка при удалении пользователя: ", error);
        }
    });
}

function saveUser(object) {
    $.ajax({
        url: "admin/user",
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(object),
        success: function (response) {
            parseAllUsers();
        },
        error: function (xhr, status, error) {
            if (xhr.status === 400) {
                let errors = xhr.responseJSON;
                let errorMessage = "Ошибки валидации:\n";
                for (let field in errors) {
                    errorMessage += `${field}: ${errors[field]}\n`;
                }
                console.error("Ошибка при сохранении пользователя: ", errorMessage);
                alert(errorMessage);
            } else {
                console.error("Ошибка при сохранении пользователя: ", error);
                alert("Ошибка при сохранении пользователя: " + error);
            }
        }
    });
}